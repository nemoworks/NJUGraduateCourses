import random
from functools import partial

import torch
import torch.nn as nn


import torchaudio

from torchvision.datasets import DatasetFolder


def audio_loader(path, max_length_in_seconds, pad_and_truncate):
    audio_tensor, sample_rate = torchaudio.load(path, normalization=True)
    max_length = sample_rate * max_length_in_seconds
    audio_size = audio_tensor.size()

    if pad_and_truncate:
        if audio_size[1] < max_length:
            difference = max_length - audio_size[1]
            padding = torch.zeros(audio_size[0], difference)
            padded_audio = torch.cat([audio_tensor, padding], 1)
            return padded_audio

        if audio_size[1] > max_length:
            random_idx = random.randint(0, audio_size[1] - max_length)
            return audio_tensor.narrow(1, random_idx, max_length)

    return audio_tensor


def get_audio_dataset(datafolder, max_length_in_seconds=1, pad_and_truncate=False):
    loader_func = partial(
        audio_loader,
        max_length_in_seconds=max_length_in_seconds,
        pad_and_truncate=pad_and_truncate,
    )
    dataset = DatasetFolder(datafolder, loader_func, ".wav")

    return dataset


class AudioCNN(nn.Module):
    def __init__(self, num_classes):
        super(AudioCNN, self).__init__()

        self.main = nn.Sequential(
            nn.Conv1d(1, 64, 80, 4, 2),
            nn.BatchNorm1d(64),
            nn.LeakyReLU(0.2, inplace=True),
            nn.Conv1d(64, 128, 80, 4, 2),
            nn.BatchNorm1d(128),
            nn.LeakyReLU(0.2, inplace=True),
            nn.Conv1d(128, 256, 80, 4, 2),
            nn.BatchNorm1d(256),
            nn.LeakyReLU(0.2, inplace=True),
            nn.Conv1d(256, 512, 80, 4, 2),
            nn.BatchNorm1d(512),
            nn.LeakyReLU(0.2, inplace=True),
            nn.Conv1d(512, 512, 40, 4, 2),
            nn.BatchNorm1d(512),
            nn.LeakyReLU(0.2, inplace=True),
        )
        self.classifier = nn.Sequential(nn.Linear(14848, num_classes), nn.Softmax())

    def forward(self, tensor):
        batch_size = tensor.size(0)
        hidden = self.main(tensor)
        # print(hidden.size())
        hidden = hidden.view(batch_size, -1)
        # print(hidden.size())
        return self.classifier(hidden)

trainset = get_audio_dataset('mydataset/Train',max_length_in_seconds=1,pad_and_truncate=True)
valset = get_audio_dataset('mydataset/Val',max_length_in_seconds=1,pad_and_truncate=True)

train_dataloader = torch.utils.data.DataLoader(trainset, batch_size=5, num_workers=4)
val_dataloader = torch.utils.data.DataLoader(valset, batch_size=5, num_workers=4)

train_dataloader_len = len(train_dataloader)
val_dataloader_len = len(val_dataloader)

audio_cnn = AudioCNN(len(trainset.classes))

use_gpu = torch.cuda.is_available()

if use_gpu:
    device = torch.device("cuda:0")
    audio_cnn = audio_cnn.to(device)

cross_entropy = torch.nn.CrossEntropyLoss()
optimizer = torch.optim.Adam(audio_cnn.parameters())

for epoch in range(1):
    audio_cnn.train()
    for sample_idx, (audio, target) in enumerate(train_dataloader):
        audio_cnn.zero_grad()
        if use_gpu:
            audio, target = audio.to(device), target.to(device)

        output = audio_cnn(audio)
        loss = cross_entropy(output, target)

        loss.backward()
        optimizer.step()

        print(
            f"{epoch:06d}-[{sample_idx + 1}/{train_dataloader_len}]: {loss.mean().item()}"
        )

    val_loss = 0
    correct = 0
    total = 0

    with torch.no_grad():
        for sample_idx, (audio, target) in enumerate(val_dataloader):
            if use_gpu:
                audio, target = audio.to(device), target.to(device)

            output = audio_cnn(audio)
            val_loss += cross_entropy(output, target)

            _, predicted = torch.max(output.data, 1)
            total += target.size(0)
            correct += (predicted == target).sum().item()

        print(f"Evaluation loss: {val_loss.mean().item() / val_dataloader_len}")
        print(f"Evaluation accuracy: {100 * correct / total}")

torch.save(audio_cnn.state_dict(),'model.pth')