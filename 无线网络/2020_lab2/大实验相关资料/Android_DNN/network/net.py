import torch
import torch.nn as nn
import torch.nn.functional as F

class SilentActionFunction(nn.Module):
  def __init__(self, threshold):
    super().__init__()
    self.threshold = nn.Parameter(torch.tensor(threshold))
  
  def forward(self, x):
    # return 1. / (1. + torch.exp(-1e-4 * (x - self.threshold)))
    return F.relu(x - self.threshold)

class SAAPredictor(nn.Module):
  def __init__(self, window, stride, seq_len):
    super().__init__()
    self.window = window
    self.seq_len = seq_len
    self.stride = stride
    self.branch_1 = nn.Sequential(
      nn.Conv2d(2, 16, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(16),
      nn.ReLU(),
      nn.MaxPool2d(2, 2),
      nn.Conv2d(16, 32, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(32),
      nn.ReLU(),
      nn.MaxPool2d(2, 2),
      nn.Conv2d(32, 16, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(16),
      nn.MaxPool2d((2, 1)),
      SilentActionFunction(1e-3))
    self.branch_2 = nn.Sequential(
      nn.Conv2d(2, 16, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(16),
      nn.ReLU(),
      nn.MaxPool2d(2, 2),
      nn.Conv2d(16, 32, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(32),
      nn.ReLU(),
      nn.MaxPool2d(2, 2),
      nn.Conv2d(32, 16, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(16),
      nn.MaxPool2d((2, 1)),
      SilentActionFunction(1e-3))
    self.branch_3 = nn.Sequential(
      nn.Conv2d(2, 32, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(32),
      nn.ReLU(),
      nn.MaxPool2d(2, 2),
      nn.Conv2d(32, 64, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(64),
      nn.ReLU(),
      nn.MaxPool2d(2, 2),
      nn.Conv2d(64, 32, kernel_size=3, padding=(1, 1)),
      nn.BatchNorm2d(32),
      nn.MaxPool2d((2, 2)),
      SilentActionFunction(1e-3))

    #to use the lite version, the transformer layer is shrinkaged to 1 and classfier is 
    self.atten = nn.TransformerEncoder(
      # nn.TransformerEncoderLayer(int(window / 8) * 64 * int(256 / 8), nhead=8),
        # num_layers=2)
      nn.TransformerEncoderLayer(int(window / 8) * 64 * int(256 / 8), nhead=8),
        num_layers=1)
    self.classifier = nn.Sequential(
      #nn.Linear(int(window / 8) * 64 * int(256 / 8) * seq_len, 512),
      nn.Linear(int(window / 8) * 64 * int(256 / 8) * seq_len, 8),
      nn.Dropout(0.5),
      nn.ReLU(), 
      # nn.Linear(512, 8),
      # nn.ReLU()
      )
  
  def forward(self, x_seq):
    '''
    x_seq: N * C * H * L format
    '''
    N, C, H, L = x_seq.size()
    print(N,C,H,L)
    # print((self.seq_len - 1) * self.stride + self.window)
    assert L == (self.seq_len - 1) * self.stride + self.window
    y_seq = []
    for idx in range(self.seq_len):
      wind_start = idx * self.stride
      wind_end = wind_start + self.window
      y1 = self.branch_1(x_seq[:, :, :, wind_start: int(wind_start + self.window / 2)])
      y2 = self.branch_2(x_seq[:, :, :, int(wind_start + self.window / 2): wind_end])
      y3 = self.branch_3(x_seq[:, :, :, wind_start: wind_end])
      # reshape to N * E
      y_seq.append(torch.cat([y1, y2, y3], dim=1).reshape(N, -1))
    # y_seq: S * N * E
    atten_out = self.atten(torch.stack(y_seq))
    # reshape to N * SE
    out = self.classifier(atten_out.permute(1, 0, 2).reshape(N, -1))
    return out
