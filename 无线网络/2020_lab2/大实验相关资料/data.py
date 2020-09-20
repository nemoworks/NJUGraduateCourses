import struct
import numpy as np
import torch
from torch.utils.data import Dataset
import os
import sys
import numpy as np
sys.path.append('.')
sys.path.append('../')
sys.path.append('../../')
from utils.utils import *

# NOTE: Deprecated
class RawData(Dataset):
  def __init__(self, file_path):
    self.file_path = file_path
    self.data = []
    self._read_file()
  
  def _read_file(self):
    # raw: len * 4 * 1024 format
    chans_counter = 0
    chans = []
    with open(self.file_path) as file:
      for idx, line in enumerate(file):
        chans.append(list(map(lambda x: float(x), line.split(','))))
        if chans_counter < 3:          
          chans_counter += 1
        else:
          self.data.append(chans)
          chans = []
          chans_counter = 0
    # turn to: Wind * 1024 * 4 format
    self.data = np.array(self.data).transpose(0, 2, 1)
    self.data = (self.data - self.data.min()) / (self.data.max() - self.data.min())
    # turn to: Wind * 1024 * 2 format
    self.data = np.array(list(map(lambda x: list(map(lambda a, b, c, d: (a**2 + b**2), \
      (c**2 + d**2) , list(x))), list(self.data))))
    # turn to: 2 * 1024 * Wind format
    self.data = self.data.transpose(2, 1, 0)

  def __len__(self):
    return len(self.data)
  
  def __get_item__(self, idx):
    return self.data[idx]

class RawData2(Dataset):
  def __init__(self, data_file_path, label_file_path):
    super().__init__()
    self.data_file_path = data_file_path
    self.label_file_path = label_file_path
    self.data = []
    self.timestamp = []
    self.label = []
    self.__read_file()
  
  def __read_file(self):
    with open(self.data_file_path, 'rb') as file:
      try:
        byte = file.read(4)
        while byte:
          self.data.append(struct.unpack('f', byte))
          byte = file.read(4)
      finally:
          file.close()
    # orig data: Wind * 4 * 1024
    # turn to: Wind * 1024 * 4 format
    self.data = np.array(self.data).reshape(-1, 4, 1024).transpose(0, 2, 1)
    self.data = np.diff(self.data, axis=0)
    chan1 = np.abs(self.data[:, :, 0] + self.data[:, :, 1] * 1j)
    chan2 = np.abs(self.data[:, :, 2] + self.data[:, :, 3] * 1j)
    self.data = np.stack([chan1, chan2], axis=-1)
    self.data = (self.data - self.data.min()) / (self.data.max() - self.data.min())
    # turn to: 2 * 1024 * Wind format
    self.data = self.data.transpose(2, 1, 0)
 
    with open(self.label_file_path, 'rb') as file:
      tmp = []
      try:
        byte = file.read(4)
        while byte:
          tmp.append(struct.unpack('<I', byte)[0])
          byte = file.read(4)
      finally:
          file.close()
      # print(tmp)
      # exit()
      lbl_len = int((len(tmp) - 1) / 2)
      self.timestamp = tmp[:lbl_len + 1]
      self.label = tmp[lbl_len + 1:]
    # exit()
    #label format: T
    self.timestamp = np.array(self.timestamp)
    self.label = np.array(self.label)
  
class SlideData(RawData2):
  def __init__(self, data_file_paths, label_file_paths, wind_size, stride_size, wind_num_per_samp, transform=None):
    super().__init__(data_file_paths[0], label_file_paths[0])
    self.data_file_paths = data_file_paths
    self.label_file_paths = label_file_paths
    self.wind_size = wind_size
    self.stride_size = stride_size
    # NOTE: b1 wind num per samp = b2 wind num per samp
    self.wind_num_per_samp = wind_num_per_samp
    self.transform = transform
  
  def next_file(self, rand_file_idx):
    # rand_file_idx = np.random.randint(len(self.data_file_paths))
    super().__init__(self.data_file_paths[rand_file_idx], self.label_file_paths[rand_file_idx])
    self.wind_num_per_samp = (self.data.shape[-1] - 32) // 8 + 1
  
  def __len__(self):
    return self.wind_num_per_samp
  
  def __getitem__(self, idx):
    stride_id = idx % self.wind_num_per_samp
    for idx, label_id in enumerate(self.timestamp):
      if label_id > stride_id * self.stride_size:
        break

    return self.data[:, 512-10:512+150, stride_id * self.stride_size: \
      stride_id * self.stride_size + self.wind_size], self.label[idx - 1]

class SlideData3(Dataset):
  def __init__(self, data_file, timestamp_file, label_file, window, stride, seq_len, training=True):
    self.data_file = data_file
    self.timestamp_file = timestamp_file
    self.label_file = label_file
    self.window = window
    self.stride = stride
    self.seq_len = seq_len
    self.training = training
    self.__load_data()
    
  def __load_data(self):
    self.data = np.load(self.data_file)
    self.timestamp = np.load(self.timestamp_file)
    self.label = np.load(self.label_file)

  def __len__(self):
    return self.data.shape[-1] - ((self.seq_len - 1) * self.stride + self.window) + 1
                                      
  def __getitem__(self, idx):
    seq_start = idx % self.__len__()
    seq_end = seq_start + ((self.seq_len - 1) * self.stride + self.window)
    label_id = next(x[0] for x in enumerate(self.timestamp) if x[1] > seq_end - self.window / 2) - 1
    seq = self.data[:, 512-32:512+224, seq_start: seq_end] 
    if self.training:
      rand_mask = np.random.randint(0, seq_end - seq_start, 2)
      seq[:, :, rand_mask] = 0.0
    if self.label[label_id] == 5:
      self.label[label_id] = 7
    return (seq - seq.min()) / (seq.max() - seq.min()), self.label[label_id]

class DataHelper:
  def __init__(self, base_path):
    self.base_path = base_path
  
  def generate_newdata(self):
    data_file = self.base_path + 'all_data.npy'
    timestamp_file = self.base_path + 'all_timestamp.npy'
    label_file = self.base_path + 'all_la bel.npy'
    data = np.load(data_file)
    timestamp = np.load(timestamp_file)
    label = np.load(label_file)

    label_slices = []
    frame_slices = []
    start_seq = False
    nzero_slice_start = -1
    for idx, lbl in enumerate(label):
      if start_seq:
        if lbl == 0:
          label_slices.append((nzero_slice_start, idx - 1))
          label_slices.append((idx, ))
          start_seq = False
      else:
        if lbl == 0:
          label_slices.append((idx, ))
        else:
          start_seq = True
          nzero_slice_start = idx
    if start_seq:
      label_slices.append((nzero_slice_start, len(label) - 1))

    for lbl_s in label_slices:
      frame_slices.append((lbl_s[0], lbl_s[-1] + 1))

    perm = np.random.permutation(len(frame_slices))
    new_data = []
    new_labels = []
    new_timestamps = [0]
    data_len = 0
    for p in perm:
      frame_num_slice = frame_slices[p]
      frame_slice = data[:, :, timestamp[frame_num_slice[0]]: timestamp[frame_num_slice[-1]]]
      new_data.append(frame_slice)
      frame_num_slice = list(range(frame_num_slice[0], frame_num_slice[-1] + 1))
      for frame_idx, frame in enumerate(frame_num_slice):
        if frame_idx != 0:
          new_timestamps.append(data_len + timestamp[frame] - timestamp[frame_num_slice[frame_idx - 1]])
          new_labels.append(label[frame_num_slice[0] + frame_idx - 1])
          data_len += timestamp[frame] - timestamp[frame_num_slice[frame_idx - 1]]
    print(data.shape)
    new_data = np.concatenate([data, np.concatenate(new_data, axis=-1)], axis=-1)
    print(new_data.shape)
    new_labels = np.array(list(label) + new_labels)
    new_timestamps = np.array(list(timestamp) + list(map(lambda x: x + data.shape[-1], new_timestamps[1:])))
    print(label.shape)
    print(new_labels.shape)
    print(timestamp.shape)
    print(new_timestamps.shape)
    with open(self.base_path + 'new_all_data.npy', 'wb') as file:
      np.save(file, new_data)
    with open(self.base_path + 'new_all_timestamp.npy', 'wb') as file:
      np.save(file, np.array(new_timestamps))
    with open(self.base_path + 'new_all_label.npy', 'wb') as file:
      np.save(file, np.array(new_labels))

  def show_dataset(self, show_label=0):
    data = np.load(self.base_path + 'all_data.npy')
    # timestamp = np.load(self.base_path + 'all_timestamp.npy').reshape(-1)
    label = np.load(self.base_path + 'all_label.npy').reshape(-1)
    idxs = np.argwhere(label == show_label).reshape(-1)
    np.random.shuffle(idxs)
    rand_idxs = idxs[:32]
    print(rand_idxs)
    for slice_idx in rand_idxs:
      img_show(data[0, 512:512+224, slice_idx: slice_idx + 56])

  def check_dataset(self):
    file_names = os.listdir(self.base_path)
    file_names.sort()
    for file_name in file_names:
      if 'label' in file_name:
        with open(self.base_path + file_name, 'rb') as file:
          tmp = []
          try:
            byte = file.read(4)
            while byte:
              tmp.append(struct.unpack('<I', byte)[0])
              byte = file.read(4)
          finally:
            file.close()

          lbl_len = int((len(tmp) - 1) / 2)
          timestamp = tmp[0:lbl_len + 1]
          label = tmp[lbl_len + 1:]
          if np.any(np.array(label) > 7):
            print(file_name)
            print(label)
            print('file len: ', len(tmp))
            print('label len: ', len(label))
            print('timestamp len: ', len(timestamp))

  def generate_dataset(self):
    file_names = os.listdir(self.base_path)
    file_names.sort()
    start_timestamp = 0
    data = []
    label = []
    timestamp = [0]
    for file_name in file_names:
      if 'cir' in file_name:
        with open(self.base_path + file_name, 'rb') as file:
          sing_file_data = []
          try:
            byte = file.read(4)
            while byte:
              sing_file_data.append(struct.unpack('f', byte))
              byte = file.read(4)
          finally:
            file.close()
          sing_file_data = np.array(sing_file_data).reshape(-1, 4, 1024).transpose(0, 2, 1)
          sing_file_data = np.diff(sing_file_data, axis=0)
          chan1 = np.abs(sing_file_data[:, :, 0] + sing_file_data[:, :, 1] * 1j)
          chan2 = np.abs(sing_file_data[:, :, 2] + sing_file_data[:, :, 3] * 1j)
          sing_file_data = np.stack([chan1, chan2], axis=-1)
          # sing_file_data = (sing_file_data - sing_file_data.min()) / (sing_file_data.max() - sing_file_data.min())
          # turn to: 2 * 1024 * Wind format
          sing_file_data = sing_file_data.transpose(2, 1, 0)
          
        for label_file_name in file_names:
          if 'label' in label_file_name and file_name.split('cir')[0] in label_file_name:
            print(file_name, ' ==> ', label_file_name)
            with open(self.base_path + label_file_name, 'rb') as file:
              tmp = []
              try:
                byte = file.read(4)
                while byte:
                  tmp.append(struct.unpack('<I', byte)[0])
                  byte = file.read(4)
              finally:
                file.close()

              lbl_len = int((len(tmp) - 1) / 2)
  
              timestamp += list(map(lambda x: start_timestamp + x - tmp[0], tmp[1:lbl_len + 1]))
              label += tmp[lbl_len + 1:]
              
              sing_file_data = sing_file_data[:, :, tmp[0]:tmp[lbl_len]]
              data.append(sing_file_data)
              # print(timestamp)
              # print(label)
              # print(file_name, label_file_name, np.concatenate(data, axis=-1).shape, \
              #   len(tmp), len(timestamp), len(label))
        start_timestamp += sing_file_data.shape[-1]
    data = np.concatenate(data, axis=-1)
    with open(self.base_path + 'all_data.npy', 'wb') as file:
      np.save(file, data)
    with open(self.base_path + 'all_timestamp.npy', 'wb') as file:
      np.save(file, np.array(timestamp))
    with open(self.base_path + 'all_label.npy', 'wb') as file:
      np.save(file, np.array(label))

  
if __name__ == '__main__':
  base_path = 'real_time/'
  dh = DataHelper(base_path)
  dh.show_dataset(3)
# exit()
# sd = SlideData3(base_path + 'all_data.npy', base_path + 'all_timestamp.npy', base_path + 'all_label.npy', 32, 8)
# print(sd.data.shape)
# print(sd.timestamp.shape)
# print(sd.label.shape)
# print(sd.timestamp)
# print(sd.label)
# wind, label = sd[10]
# print(wind.shape, label)
# print(sd.timestamp[:8])
# print(sd.label[:8])
# exit()