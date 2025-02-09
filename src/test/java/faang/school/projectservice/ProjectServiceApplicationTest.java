package faang.school.projectservice;

import faang.school.projectservice.config.TestContainersConfig;
import faang.school.projectservice.repository.TeamMemberRepository;
import faang.school.projectservice.repository.VacancyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootTest(classes = TestContainersConfig.class, properties = "spring.profiles.active=test")
public class ProjectServiceApplicationTest{

    private final Logger log = LoggerFactory.getLogger(ProjectServiceApplicationTest.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldLoadApplicationContext() {
        Assertions.assertNotNull(applicationContext, "Application context should load successfully");
    }

    @Test
    public void shouldFindSpecificBeansInContext() {
        boolean beanExists = applicationContext.containsBean("vacancyRepository");
        log.info("Bean found: {}", beanExists);
        Assertions.assertTrue(beanExists, "VacancyRepository bean should exist in the application context");

        VacancyRepository myRepository = applicationContext.getBean(VacancyRepository.class);
        log.info("VacancyRepository bean: {}", myRepository);
        Assertions.assertNotNull(myRepository, "VacancyRepository bean should not be null");

        Object vacancyRepositoryBean = applicationContext.getBean("vacancyRepository");
        log.info("VacancyRepository bean class: {}", vacancyRepositoryBean.getClass().getName());
        Assertions.assertTrue(
                VacancyRepository.class.isAssignableFrom(vacancyRepositoryBean.getClass()),
                "Bean class should implement VacancyRepository"
        );

        TeamMemberRepository teamMemberRepositoryBean = applicationContext.getBean(TeamMemberRepository.class);
        log.info("TeamMemberRepository bean: {}", teamMemberRepositoryBean);
        Assertions.assertNotNull(teamMemberRepositoryBean, "TeamMemberRepository bean should not be null");
    }

    @Test
    public void testPostgresContainerIsRunning() {
        String jdbcUrl = applicationContext.getEnvironment().getProperty("spring.datasource.url");
        Assertions.assertNotNull(jdbcUrl, "Postgres container doesnt start yet!");
        log.info("PostgresSQL URL: {}", jdbcUrl);
    }
}
