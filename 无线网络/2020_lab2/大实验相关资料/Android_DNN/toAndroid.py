import queue
import numpy as np
import torch
import torch.nn as nn
import matplotlib.pyplot as plt
from network.net import SilentActionFunction, SAAPredictor
import struct
import random


model = SAAPredictor(32,8,3)
model.load_state_dict(torch.load('ckpts/saa_pred_lite.pth', map_location='cpu'))
model.to(torch.device('cpu'))
model.float()
model.eval()
#input size
dummy_item = torch.rand(48, 2, 256)

#a = torch.rand(2,256,56)
seq = torch.tensor(dummy_item, dtype=torch.float).to('cpu')\
            .permute(1, 2, 0).unsqueeze(0)
print(torch.argmax(model.forward(seq), 1).item())

# dynamic_quan_model = torch.quantization.quantize_dynamic(
#     model, {nn.TransformerEncoderLayer, nn.Linear}, dtype=torch.qint8)


traced_script_module = torch.jit.trace(model.float(),seq)
traced_script_module.save('/Users/haoranwan/Desktop/saa_pred_lite_int.pt')