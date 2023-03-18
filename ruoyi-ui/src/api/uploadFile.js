import {requestApi} from '@/utils/video-request';
import { option } from '@/api/optionParam';

export const mergeFile = data => {
    option.isJson = true
    option.data = JSON.stringify(data)
    option.method = 'post'
    option.url = '/video/uploader/mergeFile'
    return requestApi(option)
  }

export const selectFileList = query => {
    option.isJson = true
    option.data = query
    option.method = 'get'
    option.url = '/video/uploader/selectFileList'
    return requestApi(option)
};

export const deleteFile = data => {
  option.isJson = true
  option.data = JSON.stringify(data)
  option.method = 'post'
  option.url = '/video/uploader/deleteFile'
  return requestApi(option)
}

export const downloadFile = query => {
  option.isJson = true
  option.data = query
  option.method = 'get'
  option.url = '/video/uploader/download'
  return requestApi(option)
};

export const getFileUrl = data => {
  option.isJson = true
  option.data = data
  option.method = 'post'
  option.url = '/video/uploader/getFileUrl'
  return requestApi(option)
};

