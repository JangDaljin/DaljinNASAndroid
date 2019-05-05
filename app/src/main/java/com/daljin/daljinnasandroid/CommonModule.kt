package com.daljin.daljinnasandroid


//Activity Request
const val REQUEST_LOGIN = 100
const val REQUEST_UPLOAD = 200
const val REQUEST_FILESELECT = 300
const val REQUEST_PERM = 1000

//Activity Result
const val RESULT_FINISH = 0
const val RESULT_LOGIN = 100
const val RESULT_UPLOAD = 200

//Permission Request



//Notification Channel
const val N_DOWNLOAD_ID = 6773
const val N_UPLOAD_ID = 3466
const val NC_DOWNLOAD = "DDownload"
const val NC_UPLOAD = "DUpload"

//SharePreference
const val SP_NAME = "DaljinNAS"
const val SP_KEY_WRITEMODE = "WRITEMODE"
const val SP_KEY_DOWNLOADPATH = "DOWNLOADPATH"

//Donwload FileSave Mode
const val SAVE_OVERWIRTE = 1
const val SAVE_IGNORE = 2




//DaljinNodeWebServer
//const val SERVER_URL = "http://daljin.dlinkddns.com"
const val SERVER_URL = "http://10.0.2.2:8000"

const val URL_LOGIN ="/loginNW"
const val FORM_NAME_LOGIN_ID = "ID"
const val FORM_NAME_LOGIN_PW = "PW"


const val URL_FILELIST = "/fileList"
const val FORM_NAME_FILELIST_PATH = "path"

const val URL_LOGOUT = "/logoutNW"

const val URL_DOWNLOAD = "/Download"
const val FORM_NAME_DOWNLOAD_PATH = "n_itemPath"
const val FORM_NAME_DOWNLOAD_ITEM = "n_downloadItem"
const val FORM_NAME_DOWNLOAD_TYPE = "n_itemType"

const val URL_MKDIR = "/makeDirectoryNW"
const val FORM_NAME_MKDIR_PATH = "n_makeDirectory_path"
const val FORM_NAME_MKDIR_NAME = "n_makeDirectory_Name"


const val URL_DELETE ="/DeleteNW"
const val FORM_NAME_DELETE_PATH = "n_deletePath"
const val FORM_NAME_DELETE_LIST = "n_deleteList"


const val URL_CHECKID = "/checkid"
const val FORM_NAME_CHECKID_ID = "ID"

const val URL_ADDUSER = "/adduserNW"
const val FORM_NAME_ADDUSER_ID = "ID"
const val FORM_NAME_ADDUSER_PW ="PW"
const val FORM_NAME_ADDUSER_CODE ="CODE"


const val URL_UPLOAD = "/fileUpload"
const val FORM_NAME_UPLOAD_PATH = "n_upload_path"

