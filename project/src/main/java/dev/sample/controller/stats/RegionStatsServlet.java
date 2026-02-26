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
import dev.sample.dto.RegionStatsDto;
import dev.sample.service.StatsService;
import dev.sample.util.JsonResponseUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 지역별 카드 이용금액 순위 API
 * POST /api/stats/region 대신 GET 방식 사용
 * GET /api/stats/region
 */
@WebServlet("/api/stats/region")
@Slf4j
public class RegionStatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        log.info("GET /api/stats/region");

        try {
            DataSource ds = ApplicationContextListener.getDataSource(getServletContext());
            StatsService service = new StatsService(ds);

            List<RegionStatsDto> result = service.getStatsByRegion();

            // JSON 변환
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < result.size(); i++) {
                RegionStatsDto dto = result.get(i);
                json.append("{")
                        .append("\"housSidoNm\":\"").append(JsonResponseUtil.escapeJson(dto.getHousSidoNm()))
                        .append("\",")
                        .append("\"totUseAm\":").append(dto.getTotUseAm()).append(",")
                        .append("\"crdslUseAm\":").append(dto.getCrdslUseAm()).append(",")
                        .append("\"cnfUseAm\":").append(dto.getCnfUseAm())
                        .append("}");
                if (i < result.size() - 1)
                    json.append(",");
            }
            json.append("]");

            JsonResponseUtil.sendSuccess(resp, json.toString());

        } catch (SQLException e) {
            log.error("DB 오류", e);
            JsonResponseUtil.sendError(resp, 500, "DB 오류가 발생했습니다.");
        }
    }
}
