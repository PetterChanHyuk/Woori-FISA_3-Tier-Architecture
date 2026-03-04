package dev.sample;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import dev.sample.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 톰캣(서블릿 컨테이너) 시작/종료 시 Spring IoC 컨테이너를 관리하는 리스너
 *
 * [변경 전] HikariDataSource를 직접 생성하여 ServletContext에 저장
 * [변경 후] Spring ApplicationContext를 생성하여 ServletContext에 저장
 * → 서블릿들이 이 컨텍스트에서 Service 빈을 꺼내어 사용 (Service Locator 패턴)
 */
@Slf4j
@WebListener
public class ApplicationContextListener implements ServletContextListener {

    /** Spring IoC 컨테이너 */
    private AnnotationConfigApplicationContext springContext;

    /** 서블릿들이 Spring 컨테이너를 찾을 때 사용할 키 */
    public static final String SPRING_CONTEXT_KEY = "SPRING_APPLICATION_CONTEXT";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // ★ 핵심: Spring IoC 컨테이너 가동!
        // AppConfig.java를 읽어서 @Bean(DataSource), @ComponentScan(@Service, @Repository)
        // 등
        // 모든 빈을 한 번에 조립(DI)하여 메모리에 올립니다.
        springContext = new AnnotationConfigApplicationContext(AppConfig.class);
        log.info("★ Spring IoC 컨테이너 가동 완료! 등록된 빈 수: {}", springContext.getBeanDefinitionCount());

        // 조립이 끝난 스프링 컨테이너를 서블릿들이 꺼내 쓸 수 있도록
        // 톰캣의 공용 보관함(ServletContext)에 보관합니다.
        ctx.setAttribute(SPRING_CONTEXT_KEY, springContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 톰캣이 종료될 때 스프링 컨테이너도 깔끔하게 정리합니다.
        // (내부적으로 HikariDataSource의 close()도 호출되어 DB 커넥션 풀이 안전하게 반납됩니다.)
        if (springContext != null) {
            springContext.close();
            log.info("★ Spring IoC 컨테이너 종료 완료");
        }
    }

    /**
     * 서블릿에서 Spring 컨테이너를 꺼내오는 편의 메서드 (Service Locator 패턴)
     */
    public static AnnotationConfigApplicationContext getSpringContext(ServletContext ctx) {
        return (AnnotationConfigApplicationContext) ctx.getAttribute(SPRING_CONTEXT_KEY);
    }
}
