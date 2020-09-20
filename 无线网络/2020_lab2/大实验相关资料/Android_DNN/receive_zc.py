import numpy as np 
import matplotlib.pyplot as plt
from numpy.fft import fft, ifft, fftshift, ifftshift
import matplotlib as mpl
from mpl_toolkits.mplot3d import Axes3D
import scipy.io as si 
import scipy.signal as ss
from mpl_toolkits.mplot3d import Axes3D

def smooth(a,WSZ):
  # a:原始数据，NumPy 1-D array containing the data to be smoothed
  # 必须是1-D的，如果不是，请使用 np.ravel()或者np.squeeze()转化 
  # WSZ: smoothing window size needs, which must be odd number,
  # as in the original MATLAB implementation
  out0 = np.convolve(a,np.ones(WSZ,dtype=int),'valid')/WSZ
  r = np.arange(1,WSZ-1,2)
  start = np.cumsum(a[:WSZ-1])[::2]/r
  stop = (np.cumsum(a[:-WSZ:-1])[::2]/r)[::-1]
  return np.concatenate(( start , out0, stop ))

def GenerateZC(Nzc=199, u=1, cf=0, q=0, fs=48000, f0=16000, length=1920):
    # get the zc sequence in time-domain
    n = np.arange(0,Nzc)
    zc = np.exp(1j*(np.pi*u*n*(n+cf+2*q)/Nzc))
    # get the fft(zc) and use fftshift to place zero-frequency point in the middle
    ZC = fft(zc)
    ZCs = fftshift(ZC)

    # move ZCs to the f0, 0-frequency point is on ZCs[int(Nzc/2)], 
    # we should move the int(Nzc/2) to k = 1920 * f0 / fs = 640
    ZCf = np.zeros(length, dtype=complex)
    ZCf[int(length*f0/fs-(Nzc-1)/2):int(length*f0/fs+(Nzc+1)/2)] = ZCs[0:Nzc]
    # the ZCf is not processed by fftshift here

    # conjugate processing of the ZCf to make the ifft signal is a real signal
    ZCfs = fftshift(ZCf)
    for i in range(int(length/2)+1, length):
        ZCfs[length - i] = np.conjugate(ZCfs[i])

    # ifft
    ZCfs = fftshift(ZCfs)
    ZCt = ifft(ZCfs)
    return ZCt


def ZCProcess(recv, n):
    # 单发单收
    Nzc = 199
    length = 4800
    f0 = 16000
    fs = 48000
    s1 = GenerateZC(f0=f0,length=length)
    # f = open('D:\\Codes\\Matlab\\data\\long_zc_'+str(n)+'.txt','rb')
    # recv = np.frombuffer(f.read())

    #recv = np.loadtxt('D:\\Codes\\Matlab\\data\\long_zc_'+str(n)+'.txt')
    #print(recv)
    #print(recv.dtype)
    #recv = recv[zc1]

    #plt.plot(recv)
    #plt.show()
    print(recv.shape)
    f1,f2 = ss.butter(5,[0.5,0.8],btype='bandpass',analog=False,output='ba')
    recv = ss.filtfilt(f1,f2,recv)

    windowSize = 20
    b = (1/windowSize)*np.ones(windowSize)
    a = [1]
    recv = ss.lfilter(b,a,recv)

    roww = int(len(recv)/length)

    footprint = np.zeros((roww,length),dtype=complex)

    for i in range(0,roww):
        a = recv[length*i:length*(i+1)]
        af = fft(a)
        #plt.plot(abs(af))
        #plt.show()
        ag = af[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
        s1f = fft(s1)
        s1g = s1f[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
        as1g = ag * np.conjugate(s1g)
        as1gf = np.zeros(length,dtype=complex)
        as1gf[int(length/2-(Nzc-1)/2):int(length/2+(Nzc+1)/2)] = as1g
        # 此时需不需要进行fftshift？
        as1gf = fftshift(as1gf)
        as1gft = ifft(as1gf)
        footprint[i] = as1gft
        #plt.plot(abs(as1gft))
        #plt.show()

    #plt.plot(footprint[109])
    #plt.show()
    '''
    x, y = footprint.shape

    xx = np.arange(0,x*y)
    yy = np.arange(0,y*x)
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    mapp = np.zeros([3,x*y])
    for i in range(0,x):
        for j in range(0,y):
            mapp[0][i*j] = i
            mapp[1][i*j] = j
            mapp[2][i*j] = footprint[i][j]
    # ax.scatter(mapp[0],mapp[1],mapp[2])
    # plt.show()
    '''

    si.savemat('D:\\Codes\\Matlab\\data\\long_zc_path_from_socket_'+str(n)+'_mic1.mat',{'zc1':footprint})

'''
# process in MATLAB
subfootprint = np.diff(footprint,axis=0)

#subfootprint_f = smooth(subfootprint,7)

R = np.zeros([100,4800])
#get_subex = subfootprint_f[101:201,:]
for j in range(0,4800):
    u = np.mean(subfootprint[:,j])
    y = np.std(subfootprint[:,j])
'''


def ZCProcessMulti(n, numchan):
    # 多个接收麦克风分别处理
    Nzc = 199
    length = 4800
    f0 = 16000
    fs = 48000
    s1 = GenerateZC(f0=f0,length=length)
    # f = open('D:\\Codes\\Matlab\\data\\long_zc_'+str(n)+'.txt','rb')
    # recv = np.frombuffer(f.read())

    #recv = np.loadtxt('D:\\Codes\\Matlab\\data\\long_zc_'+str(n)+'.txt')
    recv = si.loadmat('/Users/haoranwan/Documents/Codes/Python/ZC_dibeamforming/data/recvbf_distance_'+str(n)+'.mat')
    recv = recv['zc1']
    recv = recv.reshape(1,-1)
    recv = recv[0]

    print(recv.shape)
    recv_chan = np.zeros([numchan,int(len(recv)/numchan)],dtype=np.float32)
    clo = 0
    for i in range(0,len(recv)):
        recv_chan[i%numchan,clo] = recv[i]
        if i%numchan == numchan-1:
            clo += 1
    

    recv_beamforming = (recv_chan[0] + recv_chan[1] + np.roll(recv_chan[2],5) + np.roll(recv_chan[5],6) + np.roll(recv_chan[3],11) + np.roll(recv_chan[4],11))/6

    #receiver beamforming here..
    #the delay and sum method..
    plt.figure()
    for i in range(0,6):
        plt.plot(recv_chan[i])
    plt.figure()
    plt.plot(recv_beamforming)
    plt.show()

    roww = int(len(recv_chan[0])/length)

    footprint = np.zeros((numchan,roww,length),dtype=complex)
    
    for j in range(0,numchan):
        for i in range(0,roww):
            a = recv_chan[j,length*i:length*(i+1)]
            af = fft(a)
            #plt.plot(abs(af))
            #plt.show()
            ag = af[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
            s1f = fft(s1)
            s1g = s1f[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
            as1g = ag * np.conjugate(s1g)
            #plt.plot(abs(ifft(as1g)))
            #plt.show()
            as1gf = np.zeros(length,dtype=complex)
            as1gf[int(length/2-(Nzc-1)/2):int(length/2+(Nzc+1)/2)] = as1g
            # 此时需不需要进行fftshift？
            as1gf = fftshift(as1gf)
            as1gft = ifft(as1gf)
            footprint[j,i] = as1gft
    plt.plot(abs(as1gft))
    plt.show()

    #plt.plot(footprint[109])
    #plt.show()
    for i in range(0,numchan):
        #print(footprint[i].shape)
        si.savemat('/Users/haoranwan/Documents/Codes/Python/ZC_dibeamforming/data/recvbf_distance_path_'+str(n)+'.mat',{'zc1':footprint[i]})


def ZCProcessMultiBFCross(n,numchan):
    # 两个发送端发送不同的载波的ZC来互相关
    Nzc = 199
    length = 4800
    f0 = 16000
    f1 = 18000
    fs = 48000
    # f = open('D:\\Codes\\Matlab\\data\\long_zc_'+str(n)+'.txt','rb')
    # recv = np.frombuffer(f.read())
    # recv = np.loadtxt('D:\\Codes\\Matlab\\data\\long_zc_'+str(n)+'.txt')
    recv = si.loadmat('D:\\Codes\\Matlab\\data\\long_zc_from_socket_'+str(n)+'.mat')
    recv = recv['zc1']
    recv = recv.reshape(1,-1)
    recv = recv[0]

    recv_chan = np.zeros([numchan,int(len(recv)/numchan)],dtype=np.float32)
    clo = 0
    for i in range(0,len(recv)):
        recv_chan[i%numchan,clo] = recv[i]
        if i%numchan == numchan-1:
            clo += 1
    
    #beamforming here..
    recv_beamforming = (recv_chan[0] + recv_chan[1] + np.roll(recv_chan[2],5) + np.roll(recv_chan[5],6) + np.roll(recv_chan[3],11) + np.roll(recv_chan[4],11))/6
   
    roww = int(len(recv_chan[0])/length)

    footprint = np.zeros((roww,length),dtype=complex)
    
    for i in range(0,roww):
        a = recv_beamforming[length*i:length*(i+1)]
        af = fft(a)
        ag = af[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
        sg = af[int(length/(fs/f1)-(Nzc-1)/2):int(length/(fs/f1)+(Nzc+1)/2)]
        as1g = ag * np.conjugate(sg)
        as1gf = np.zeros(length,dtype=complex)
        as1gf[int(length/2-(Nzc-1)/2):int(length/2+(Nzc+1)/2)] = as1g
        as1gf = fftshift(as1gf)
        as1gft = ifft(as1gf)
        footprint[i] = as1gft

    si.savemat('/Users/haoranwan/Documents/Codes/Python/ZC_dibeamforming/data/recvbf_distance_'+str(n)+'.mat',{'zc1':footprint})


def ZCProcessMultiRecvBF(n,numchan, f0):
    # 单发多收
    Nzc = 199
    length = 4800
    #f0 = 18000
    #f0 = 15000
    fs = 48000
    s1 = GenerateZC(f0=f0,length=length)
    # f = open('D:\\Codes\\Matlab\\data\\long_zc_'+str(n)+'.txt','rb')
    # recv = np.frombuffer(f.read())

    # recv = np.loadtxt('D:\\Codes\\Matlab\\data\\long_zc_'+str(n)+'.txt')
    recv = si.loadmat('/Users/haoranwan/Documents/Codes/Python/ZC_dibeamforming/data/recvbf_distance_1m_'+str(n)+'.mat')
    recv = recv['zc1']
    recv = recv.reshape(1,-1)
    recv = recv[0]

    print(recv.shape)
    recv_chan = np.zeros([numchan,int(len(recv)/numchan)],dtype=np.float32)
    clo = 0
    for i in range(0,len(recv)):
        recv_chan[i%numchan,clo] = recv[i]
        if i%numchan == numchan-1:
            clo += 1
    
    recv_beamforming = (recv_chan[0] + recv_chan[1] + np.roll(recv_chan[2],5) + np.roll(recv_chan[5],6) + np.roll(recv_chan[3],11) + np.roll(recv_chan[4],11))/6
    #ZCProcess(recv_chan[0],n)
    #receiver beamforming here..
    #the delay and sum method..
    #plt.figure()
    #plt.plot(recv_beamforming)
    #plt.show()
    roww = int(len(recv_chan[0])/length)

    footprint = np.zeros((roww,length),dtype=complex)
    
    for i in range(0,roww):
        a = recv_beamforming[length*i:length*(i+1)]
        af = fft(a)
        #plt.plot(abs(af))
        #plt.show()
        ag = af[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
        s1f = fft(s1)
        s1g = s1f[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
        as1g = ag * np.conjugate(s1g)
        #plt.plot(abs(ifft(as1g)))
        #plt.show()
        as1gf = np.zeros(length,dtype=complex)
        as1gf[int(length/2-(Nzc-1)/2):int(length/2+(Nzc+1)/2)] = as1g
        # 此时需不需要进行fftshift？
        as1gf = fftshift(as1gf)
        as1gft = ifft(as1gf)
        footprint[i] = as1gft
    #plt.plot(abs(as1gft))
    #plt.show()

    #plt.plot(footprint[109])
    #plt.show()
    '''
    row1, colunm1 = footprint.shape
    fig = plt.figure()
    ax = Axes3D(fig)
    X = np.arange(0,row1,1)
    Y = np.arange(0,colunm1,1)
    Y, X = np.meshgrid(Y,X)
    
    #print(X.shape, Y.shape, footprint.shape)

    ax.plot_surface(X, Y, footprint, rstride=1, cstride=1, cmap='rainbow')

    plt.show()
    '''
    si.savemat('/Users/haoranwan/Documents/Codes/Python/ZC_dibeamforming/data/recvbf_distance_path_1m_'+str(n)+'.mat',{'zc1':footprint})
    

if __name__ == '__main__':
    try:
        #ZCProcess(4)
        ZCProcessMultiRecvBF(57,6,18000)
    except KeyboardInterrupt:
        print('Done by user.')

