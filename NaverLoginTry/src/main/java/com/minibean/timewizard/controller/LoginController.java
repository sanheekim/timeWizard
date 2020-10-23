package com.minibean.timewizard.controller;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minibean.timewizard.model.biz.UserInfoBiz;
import com.minibean.timewizard.model.dto.UserInfoDto;
import com.minibean.timewizard.utils.LoginGoogleVO;
import com.minibean.timewizard.utils.LoginKakaoVO;
import com.minibean.timewizard.utils.LoginNaverVO;

@Controller
@RequestMapping(value = "/login")
public class LoginController {

    @Autowired
    private UserInfoBiz userInfoBiz;
    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginNaverVO loginNaverVO;
    @Autowired
    private LoginGoogleVO loginGoogleVO;
    @Autowired
    private LoginKakaoVO loginKakaoVO;
    private String apiResult = null;

    @Autowired
    private void setLoginNaverVO(LoginNaverVO loginNaverVO) {
        this.loginNaverVO = loginNaverVO;
    }

    /* 기본 로그인창 */
    @RequestMapping(value = "", method = { RequestMethod.GET, RequestMethod.POST })
    public String loginPage(Model model, HttpSession session) {
        logger.info(">> [CONTROLLER-USERINFO] move to login page");

        String naverOAuthUrl = loginNaverVO.getAuthorizationUrl(session);
        String googleOAuthUrl = loginGoogleVO.getAuthorizationUrl(session);
        String kakaoOAuthUrl = loginKakaoVO.getAuthorizationUrl(session);
        model.addAttribute("naver_url", naverOAuthUrl);
        model.addAttribute("google_url", googleOAuthUrl);
         model.addAttribute("kakao_url", kakaoOAuthUrl);
        logger.info("* naver: " + naverOAuthUrl);
        logger.info("* google: " + googleOAuthUrl);
        logger.info("* kakao : " + kakaoOAuthUrl);

        return "userlogin";
    }

    // 일반 로그인 ID 혹은 PW를 입력하지 않았거나 틀렸을 때 (userlogin.jsp의 javascript와 연결)
    @RequestMapping(value = "/ajaxlogin", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Boolean> ajaxLogin(@RequestBody UserInfoDto dto, HttpSession session) {
        logger.info("[ajaxlogin]");

        UserInfoDto res = userInfoBiz.selectOne(dto);

        boolean check = false;
        if (res != null) {
            // 로그인 값을 계속 가지고 있는 Session
            session.setAttribute("login", res);
            check = true;
        }

        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("check", check);

        return map;
    }

    // 일반 로그인에서 아이디/PW를 맞게 입력했을 때 넘어감
    @RequestMapping(value = "/success")
    public String success(UserInfoDto dto, HttpSession session) {

        return "redirect:../success";

    }

    // 로그아웃 시 세션 제거
    @RequestMapping(value = "/logout")
    public String logout(UserInfoDto dto, HttpSession session) {

        session.invalidate();
        return "index";

    }

    /* sns 로그인 - NAVER */
    @RequestMapping(value = "/navercallback", method = { RequestMethod.GET, RequestMethod.POST })
    public String navercallback(@RequestParam String code, @RequestParam String state, HttpSession session)
            throws IOException, InterruptedException, ExecutionException {

        logger.info(">> [CONTROLLER-USERINFO] NAVER callback ");

        OAuth2AccessToken oauthToken = loginNaverVO.getAccessToken(session, code, state);
        apiResult = loginNaverVO.getUserProfile(oauthToken);

        JsonObject object = JsonParser.parseString(apiResult).getAsJsonObject().get("response").getAsJsonObject();

        UserInfoDto naverdto = new UserInfoDto();
        naverdto.setUser_id(object.get("id").toString().split("\"")[1]);
        naverdto.setUser_pw("naver"); // 해당 부분 수정 바람
        naverdto.setUser_email(object.get("email").toString().split("\"")[1]);
        naverdto.setUser_name(object.get("name").toString().split("\"")[1]);

        UserInfoDto result = userInfoBiz.selectOne(naverdto);
        if (result == null) {
            session.setAttribute("snsinfo", naverdto);
            return "redirect:./snssignup";
        } else {
            session.setAttribute("login", result);
            return "redirect:../success";
        }
    }

    /* sns 로그인 - GOOGLE */
    @RequestMapping(value = "/googlecallback", method = { RequestMethod.GET, RequestMethod.POST })
    public String googlecallback(@RequestParam String code, @RequestParam String state, HttpSession session)
            throws IOException, InterruptedException, ExecutionException {
        logger.info(">> [CONTROLLER-USERINFO] GOOGLE callback");
        OAuth2AccessToken oauthToken = loginGoogleVO.getAccessToken(session, code, state);
        apiResult = loginGoogleVO.getUserProfile(oauthToken);
        // logger.info("* api result : " + apiResult);

        JsonObject object = JsonParser.parseString(apiResult).getAsJsonObject();
        UserInfoDto googledto = new UserInfoDto();
        googledto.setUser_id(object.get("sub").toString().split("\"")[1]);
        googledto.setUser_pw("google"); // 해당 부분 수정 바람
        googledto.setUser_email(object.get("email").toString().split("\"")[1]);
        googledto.setUser_name(object.get("name").toString().split("\"")[1]);

        UserInfoDto result = userInfoBiz.selectOne(googledto);
        if (result == null) {
            session.setAttribute("snsinfo", googledto);
            return "redirect:./snssignup";
        } else {
            session.setAttribute("login", result);
            return "redirect:../success";
        }
    }

    
    @RequestMapping(value = "/kakaocallback", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public String kakaocallback(@RequestBody String json, HttpSession session) throws IOException, InterruptedException, ExecutionException {
        logger.info(">> [CONTROLLER-USERINFO] KAKAO callback");
        
        JsonObject apiResultObject = JsonParser.parseString(json).getAsJsonObject();
        JsonObject kakaoAccountObject = JsonParser.parseString(apiResultObject.get("kakao_account").toString()).getAsJsonObject();
        JsonObject profileObject = JsonParser.parseString(kakaoAccountObject.get("profile").toString()).getAsJsonObject();
        
        UserInfoDto kakaodto = new UserInfoDto();
        kakaodto.setUser_id(apiResultObject.get("id").toString());
        kakaodto.setUser_pw("kakao");
        kakaodto.setUser_name(profileObject.get("nickname").toString().split("\"")[1]);
        kakaodto.setUser_email(kakaoAccountObject.get("email").toString().split("\"")[1]);
        
        UserInfoDto result = userInfoBiz.selectOne(kakaodto);
        if (result == null) {
            session.setAttribute("snsinfo", kakaodto);
            return "/timewizard/login/snssignup";
        } else {
            session.setAttribute("login", result);
            return "/timewizard/login/kakaosuccess";
        }
    }
    
    @RequestMapping(value = "/kakaosuccess")
    public String success(HttpSession session) {

        return "redirect:../success";

    }

    
    /* sns 회원가입 */
    @RequestMapping(value = "/snssignup")
    public String signupPageSns(Model model, UserInfoDto dto) {
        logger.info(">> [CONTROLLER-USERINFO] move to user signup form (SNS)");

        return "usersignupsns";
    }

    /* 일반 회원가입 */
    @RequestMapping(value = "/signup")
    public String signupPage() {
        logger.info(">> [CONTROLLER-USERINFO] move to user signup form");

        return "usersignup";
    }

    /* 회원가입 완료 */
    @RequestMapping(value = "/signupresult")
    public String signupResult(UserInfoDto dto) {
        logger.info(">> [CONTROLLER-USERINFO] signup");

        int res = userInfoBiz.insert(dto);
        if (res > 0) {
            return "redirect:../login";
        } else {
            logger.info("[ERROR] CONTROLLER-USERINFO :: signup form");
            return "redirect:../login";
        }

    }

}
