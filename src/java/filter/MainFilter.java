package filter;

import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.URLConstants;

/**
 *
 * @author HOME
 */
public class MainFilter implements Filter {

    private final String BACKSLASH = "/";
    private final String[] LEGAL_URL_FOR_ADMIN = {
        URLConstants.SEARCH_PAGE,
        URLConstants.LOG_OUT,
        URLConstants.CREATE_NEW_USER_PAGE,
        URLConstants.CREATE_NEW_USER_REQUEST,
        URLConstants.CHANGE_USER_STATUS_REQUEST,
        URLConstants.UPDATE_USER_PAGE,
        URLConstants.UPDATE_USER_REQUEST,
        URLConstants.PROMOTION_VIEW_PAGE,
        URLConstants.VIEW_PROMOTION_REQUEST,
        URLConstants.ADD_TO_PROMOTION_REQUEST,
        URLConstants.REMOVE_USER_FROM_PROMOTION_REQUEST,
        URLConstants.UPDATE_USER_RANK_REQUEST};
    private final String[] LEGAL_URL_FOR_STUDENT = {
        URLConstants.SEARCH_PAGE,
        URLConstants.LOG_OUT,};

    private static final boolean debug = true;

    private FilterConfig filterConfig = null;

    public MainFilter() {
    }

    private void doBeforeProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("MainFilter:DoBeforeProcessing");
        }
    }

    private void doAfterProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("MainFilter:DoAfterProcessing");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        String uri = ((HttpServletRequest) request).getRequestURI();
        if (uri.indexOf("/images") > 0) {
            chain.doFilter(request, response);
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);

        String URI = httpRequest.getRequestURI();

        //Logout -> xóa hết session
//        checkLogoutRequest(request, response, URI, session);
        if (URI.endsWith(URLConstants.LOG_OUT)) {
            session.removeAttribute("userinfo");
        }

        //Condition variable 
        boolean isLoggedIn = (session != null && session.getAttribute("userinfo") != null);
        String loginURI = httpRequest.getContextPath() + BACKSLASH + URLConstants.LOGIN_REQUEST;
        boolean isLoginRequest = httpRequest.getRequestURI().equals(loginURI);
        boolean isLoginPage = httpRequest.getRequestURI().endsWith(URLConstants.LOGIN_PAGE);
        boolean isSignupPage = httpRequest.getRequestURI().endsWith(URLConstants.SIGN_UP_REQUEST);

        if (isLoggedIn && (isLoginRequest || isLoginPage)) {
            //login roi mà muốn login lại thì sẽ vào home page
            HttpSession session2 = httpRequest.getSession(false);

            RequestDispatcher dispatcher = request.getRequestDispatcher(URLConstants.SEARCH_REQUEST);
            dispatcher.forward(request, response);
        } else if (isLoggedIn) {
            //khi login roi
            HttpSession session2 = httpRequest.getSession(false);
            User userinfo = (User) session2.getAttribute("userinfo");
            System.out.println("role:  " + userinfo.getRole());
            if (userinfo != null
                    && "admin".equals(userinfo.getRole().getName())) {
                System.out.println("Filter -> role admin");
                String currentPath = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length() + 1);
                System.out.println("url " + currentPath);
                if (Arrays.asList(LEGAL_URL_FOR_ADMIN).contains(currentPath)) { //Cac Request/Page Admin co the truy cap
                    System.out.println("here");
                    chain.doFilter(request, response);
                } else {
                    //Cac Request/Page lạ thì sẽ vào luôn redirect đên trang search-quiz.jsp
//                    httpResponse.sendRedirect("");
                    RequestDispatcher dispatcher = request.getRequestDispatcher(URLConstants.SEARCH_REQUEST);
                    dispatcher.forward(request, response);
                }
            } else {
                //cac role khac, o day la role student
                //kiem tra url ton tai thi cho phep tiep tuc
                System.out.println("Filter -> role Student");
                String currentPath = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length() + 1);
                if (Arrays.asList(LEGAL_URL_FOR_STUDENT).contains(currentPath)) {
                    chain.doFilter(request, response);
                } else {
                    //neu khong -> luon luon se vao trang search
                    RequestDispatcher dispatcher = request.getRequestDispatcher(URLConstants.SEARCH_REQUEST);
                    dispatcher.forward(request, response);
                }
            }
        } else {
            System.out.println("Login: False");
            //Khi chưa login
            if (isLoginPage) {
                RequestDispatcher dispatcher = request.getRequestDispatcher(URLConstants.LOGIN_REQUEST);
                dispatcher.forward(request, response);
            } else if (isSignupPage) {
                System.out.println("isSignUpPage");
                RequestDispatcher dispatcher = request.getRequestDispatcher(URLConstants.SIGN_UP_REQUEST);
                dispatcher.forward(request, response);
            } else {
                RequestDispatcher dispatcher = request.getRequestDispatcher(URLConstants.LOGIN_REQUEST);
                dispatcher.forward(request, response);
            }
        }
    }

    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("MainFilter:Initializing filter");
            }
        }
    }

    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("MainFilter()");
        }
        StringBuilder sb = new StringBuilder("MainFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (IOException ex) {
        }
        return stackTrace;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }

    private void checkLogoutRequest(ServletRequest request, ServletResponse response, String URI, HttpSession session)
            throws ServletException, IOException {
        //Logout -> xóa hết session
        if (URI.endsWith(URLConstants.LOG_OUT)) {
            session.removeAttribute("userinfo");
            HttpServletResponse httpresponse = (HttpServletResponse) response;
            httpresponse.sendRedirect(URLConstants.LOGIN_PAGE);
        }
    }
}