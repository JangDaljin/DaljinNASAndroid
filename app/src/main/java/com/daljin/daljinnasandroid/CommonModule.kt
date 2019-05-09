package com.daljin.daljinnasandroid


//Activity Request
const val REQUEST_LOGIN = 100
const val REQUEST_UPLOAD = 200
const val EXTRA_UPLOAD_TOTALSIZE = "UPLOAD_TOTAL_SIZE"
const val EXTRA_UPLOAD_FILES = "UPLOAD_FILES"
const val REUPLOADLIST = "REUPLOADLIST"
const val REUPLOADERRORMSG = "REUPLOADERRORMSG"
const val REQUEST_FILESELECT = 400
const val REQUEST_PERM = 1000
const val REQUEST_PENDING = 9999


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
//const val SERVER_URL = "http://daljin.dlinkddns.com" // RELEASE
const val SERVER_URL = "http://10.0.2.2:8000"      // TEST

const val URL_NAVER_LOGIN= "/validatetoken"
const val NAVER_TOKEN = "token"

const val URL_SESSION_CHECK = "/sessioncheck"

const val URL_FILELIST = "/filelist"
const val FORM_NAME_FILELIST_PATH = "path"

const val URL_LOGOUT = "/logout"

const val URL_DOWNLOAD = "/download"
const val FORM_NAME_DOWNLOAD_PATH = "n_itemPath"
const val FORM_NAME_DOWNLOAD_ITEM = "n_downloadItem"
const val FORM_NAME_DOWNLOAD_TYPE = "n_itemType"

const val URL_MKDIR = "/mkdir"
const val FORM_NAME_MKDIR_PATH = "mkdirPath"
const val FORM_NAME_MKDIR_NAME = "mkdirName"


const val URL_DELETE ="/delete"
const val FORM_NAME_DELETE_PATH = "daletePath"
const val FORM_NAME_DELETE_LIST = "deleteList"


const val URL_CHECKID = "/checkid"
const val FORM_NAME_CHECKID_ID = "ID"

const val URL_ADDUSER = "/adduser"
const val FORM_NAME_ADDUSER_ID = "ID"
const val FORM_NAME_ADDUSER_PW ="PW"
const val FORM_NAME_ADDUSER_CODE ="CODE"


const val URL_UPLOAD = "/upload"
const val FORM_NAME_UPLOAD_PATH = "n_upload_path"

