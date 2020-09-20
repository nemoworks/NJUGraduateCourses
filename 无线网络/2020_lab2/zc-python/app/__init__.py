from flask import Flask

# UPLOAD_FOLDER = '/path/to/the/uploads'
UPLOAD_FOLDER = '/Users/wangguochang/Desktop/课程归档/无线网络/大实验相关资料/zc-python/mydataset'


app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

from app import routes