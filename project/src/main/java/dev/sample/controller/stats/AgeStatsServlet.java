package dev.sample.controller.stats;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.sample.ApplicationContextListener;
import dev.sample.dto.AgeStatsDto;
import dev.sample.service.StatsService;
import dev.sample.util.JsonResponseUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 연령대별 업종 소비 통계 API
 * GET /api/stats/age?age=30
 */
@WebServlet("/api/stats/age")
@Slf4j
public class AgeStatsServlet extends HttpServlet {

    private StatsService statsService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // ★ 핵심 변경: 매 요청마다 new StatsService(ds) 하던 것을
        // init()에서 단 1번만 스프링 컨테이너에서 꺼내어 재사용합니다!
        statsService = ApplicationContextListener
                .getSpringContext(config.getServletContext())
                .getBean(StatsService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String age = req.getParameter("age");
        log.info("GET /api/stats/age - age={}", age);

        try {
            List<AgeStatsDto> result = statsService.getStatsByAge(age);

            // JSON 변환
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < result.size(); i++) {
                AgeStatsDto dto = result.get(i);
                json.append("{")
                        .append("\"age\":\"").append(dto.getAge()).append("\",")
                        .append("\"fsbzAm\":").append(dto.getFsbzAm()).append(",")
                        .append("\"trvlecAm\":").append(dto.getTrvlecAm()).append(",")
                        .append("\"distAm\":").append(dto.getDistAm()).append(",")
                        .append("\"insuHosAm\":").append(dto.getInsuHosAm()).append(",")
                        .append("\"clothGdsAm\":").append(dto.getClothGdsAm()).append(",")
                        .append("\"autoAm\":").append(dto.getAutoAm()).append(",")
                        .append("\"interiorAm\":").append(dto.getInteriorAm()).append(",")
                        .append("\"offEduAm\":").append(dto.getOffEduAm()).append(",")
                        .append("\"plSanitAm\":").append(dto.getPlSanitAm())
                        .append("}");
                if (i < result.size() - 1)
                    json.append(",");
            }
            json.append("]");

            JsonResponseUtil.sendSuccess(resp, json.toString());

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청 - {}", e.getMessage());
            JsonResponseUtil.sendError(resp, 400, e.getMessage());
        } catch (SQLException e) {
            log.error("DB 오류", e);
            JsonResponseUtil.sendError(resp, 500, "DB 오류가 발생했습니다.");
        }
    }
}
