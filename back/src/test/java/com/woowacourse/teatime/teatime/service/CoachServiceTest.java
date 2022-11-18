package com.woowacourse.teatime.teatime.service;

import static com.woowacourse.teatime.teatime.fixture.DomainFixture.getCoachJason;
import static com.woowacourse.teatime.teatime.fixture.DomainFixture.getCoachMe;
import static com.woowacourse.teatime.teatime.fixture.DomainFixture.getCrew;
import static com.woowacourse.teatime.teatime.fixture.DtoFixture.COACH_BROWN_SAVE_REQUEST;
import static com.woowacourse.teatime.teatime.fixture.DtoFixture.COACH_JUNE_SAVE_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.woowacourse.teatime.teatime.controller.dto.request.CoachUpdateProfileRequest;
import com.woowacourse.teatime.teatime.controller.dto.response.CoachFindResponse;
import com.woowacourse.teatime.teatime.controller.dto.response.CoachProfileResponse;
import com.woowacourse.teatime.teatime.domain.Coach;
import com.woowacourse.teatime.teatime.domain.Crew;
import com.woowacourse.teatime.teatime.repository.CoachRepository;
import com.woowacourse.teatime.teatime.repository.CrewRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class CoachServiceTest {

    @Autowired
    private CoachService coachService;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private CrewRepository crewRepository;

    @DisplayName("코치 목록을 조회한다.")
    @Test
    void findAll() {
        coachService.save(COACH_BROWN_SAVE_REQUEST);
        coachService.save(COACH_JUNE_SAVE_REQUEST);
        List<CoachFindResponse> coaches = coachService.findAll();

        assertThat(coaches.size()).isEqualTo(2);
    }

    @DisplayName("나를 포함한 코치 목록을 조회한다.")
    @Test
    void findByName() {
        coachService.save(COACH_BROWN_SAVE_REQUEST);
        coachService.save(COACH_JUNE_SAVE_REQUEST);
        Crew crew = crewRepository.save(getCrew());
        coachRepository.save(getCoachMe(crew.getName()));

        List<CoachFindResponse> coaches = coachService.findByName(crew.getId());

        assertAll(
                () -> assertThat(coaches.size()).isEqualTo(4),
                () -> assertThat(coaches.get(0).getName()).isEqualTo("마루")
        );
    }

    @DisplayName("자신의 프로필을 수정한다.")
    @Test
    void updateProfile() {
        // given
        Coach coach = coachRepository.save(getCoachJason());

        // when
        String expectedDescription = "안뇽하세요.";
        coachService.updateProfile(coach.getId(),
                new CoachUpdateProfileRequest(getCoachJason().getName(), expectedDescription, true));

        // then
        assertThat(coach.getDescription()).isEqualTo(expectedDescription);
    }

    @DisplayName("자신의 프로필을 조회한다.")
    @Test
    void getProfile() {
        // given
        Coach coach = coachRepository.save(getCoachJason());

        // when
        CoachProfileResponse response = coachService.getProfile(coach.getId());

        // then
        assertAll(
                () -> assertThat(response.getName()).isEqualTo("제이슨"),
                () -> assertThat(response.getImage()).isEqualTo("image"),
                () -> assertThat(response.getDescription()).isEqualTo("안녕하세요~ 제이슨입니다:)")
        );
    }
}
