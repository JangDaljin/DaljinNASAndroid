package com.daljin.daljinnasandroid

import android.content.Context
import android.widget.Toast
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState


const val NAVER_CLIENT_ID = "1YgY8mfRkWeMrJJijjWA"
const val NAVER_CLIENT_SECRET = "vdwkDUS14Q"
const val NAVER_CLIENT_NAME = "DaljinNAS"

val NaverLoginModule : OAuthLogin = OAuthLogin.getInstance()

class NaverLoginInfo
{
    companion object {
        var isLogin = false
        var accessToken : String? = null
        var refreshToken : String? = null
        var expiresAt : Long? = null
        var tokenType : String? = null
        var stage : OAuthLoginState? = null
        var errorCode : String? = null
        var errorDesc : String? = null
    }
}

class NaverLoginHandler(var context : Context , var callback : ((Boolean)->Unit)?) : OAuthLoginHandler() {

    override fun run(success: Boolean) {
        if (success) {
            NaverLoginInfo.accessToken = NaverLoginModule.getAccessToken(context)
            NaverLoginInfo.refreshToken = NaverLoginModule.getRefreshToken(context)
            NaverLoginInfo.expiresAt = NaverLoginModule.getExpiresAt(context)
            NaverLoginInfo.tokenType = NaverLoginModule.getTokenType(context)
            NaverLoginInfo.stage = NaverLoginModule.getState(context)
            NaverLoginInfo.isLogin = true
        }
        else {
            NaverLoginInfo.errorCode = NaverLoginModule.getLastErrorCode(context).code
            NaverLoginInfo.errorDesc = NaverLoginModule.getLastErrorDesc(context)
            NaverLoginInfo.isLogin = false
            Toast.makeText(
                context, "errorCode:" + NaverLoginInfo.errorCode
                        + ", errorDesc:" + NaverLoginInfo.errorDesc, Toast.LENGTH_SHORT
            ).show()
        }
        callback?.invoke(success)
    }
}

