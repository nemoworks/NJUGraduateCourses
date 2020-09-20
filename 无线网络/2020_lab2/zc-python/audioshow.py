import torch
import torchaudio
import matplotlib.pyplot as plt
filename ="/Users/wangguochang/Desktop/课程归档/无线网络/大实验相关资料/zc-python/mydataset/Train/0/1.wav"
waveform, sample_rate = torchaudio.load(filename)
print("Shape of waveform: {}".format(waveform.size()))
print("Sample rate of waveform: {}".format(sample_rate))
plt.figure()
plt.plot(waveform.t().numpy())
plt.show()