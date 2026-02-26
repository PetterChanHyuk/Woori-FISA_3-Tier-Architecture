package dev.sample.controller.stats;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import dev.sample.ApplicationContextListener;
import dev.sample.dto.LifestageStatsDto;
import dev.sample.service.StatsService;
import dev.sample.util.JsonResponseUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 라이프스테이지별 소비 패턴 API
 * POST /api/stats/lifestage 대신 GET 방식 사용
 * GET /api/stats/lifestage?lifeStage=NEW_WED
 */
@WebServlet("/api/stats/lifestage")
@Slf4j
public class LifestageStatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String lifeStage = req.getParameter("lifeStage");
        log.info("GET /api/stats/lifestage - lifeStage={}", lifeStage);

        try {
            DataSource ds = ApplicationContextListener.getDataSource(getServletContext());
            StatsService service = new StatsService(ds);

            List<LifestageStatsDto> result = service.getStatsByLifestage(lifeStage);

            // JSON 변환
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < result.size(); i++) {
                LifestageStatsDto dto = result.get(i);
                json.append("{")
                        .append("\"lifeStage\":\"").append(JsonResponseUtil.escapeJson(dto.getLifeStage()))
                        .append("\",")
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
