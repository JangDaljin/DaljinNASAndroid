package com.daljin.daljinnasandroid


//Activity Request
const val REQUEST_LOGIN = 100
const val REQUEST_UPLOAD = 200
const val REQUEST_CODE_UPDATE = 300
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
const val RESULT_CODE_UPDATE= 300

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
const val SERVER_URL = "http://daljin.dlinkddns.com" // RELEASE
//const val SERVER_URL = "http://10.0.2.2:8000"      // TEST

const val URL_NAVER_LOGIN= "/navertokenlogin"
const val NAVER_TOKEN = "access_token"

const val URL_SESSION_CHECK = "/sessioncheck"

const val URL_FILELIST = "/cloud/filelist"
const val FORM_NAME_FILELIST_PATH = "path"

const val URL_LOGOUT = "/logout"

const val URL_DOWNLOAD = "/cloud/download"
const val FORM_NAME_DOWNLOAD_PATH = "path"
const val FORM_NAME_DOWNLOAD_ITEM = "item"
const val FORM_NAME_DOWNLOAD_TYPE = "type"

const val URL_MKDIR = "/cloud/mkdir"
const val FORM_NAME_MKDIR_PATH = "mkdirPath"
const val FORM_NAME_MKDIR_NAME = "mkdirName"


const val URL_DELETE ="/cloud/delete"
const val FORM_NAME_DELETE_PATH = "daletePath"
const val FORM_NAME_DELETE_LIST = "deleteList"


const val URL_USER_INFO_UPADTE = "/userinfoupdate"
const val FORM_NAME_USER_INFO_UPDATE_CODE ="code"
const val FORM_NAME_USER_INFO_UPDATE_NICKNAME = "nickname"


const val URL_UPLOAD = "/cloud/upload"
const val FORM_NAME_UPLOAD_PATH = "path"
const val FORM_NAME_UPLOAD_FILES = "files"

