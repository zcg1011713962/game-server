package game.hall.interceptor;

import game.common.constant.ErrorCode;
import game.common.context.UserContext;
import game.common.protocol.ServerMsg;
import game.common.util.JsonUtil;
import game.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            return writeUnauthorized(response);
        }
        Long userId = JwtUtil.getUserId(token);
        if (userId == null) {
            return writeUnauthorized(response);
        }
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.clear();
    }


    /**
     * 返回 401 JSON 给客户端
     */
    private boolean writeUnauthorized(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        try (PrintWriter writer = response.getWriter()) {
            writer.write(JsonUtil.toJson(ServerMsg.error(ErrorCode.NOT_AUTH)));
            writer.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false; // 阻止继续调用 Controller
    }
}
