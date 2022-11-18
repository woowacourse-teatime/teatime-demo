package com.woowacourse.teatime.teatime.domain;

import static com.woowacourse.teatime.teatime.fixture.DomainFixture.getCoachJason;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoachTest {

    @DisplayName("콕 찔러보기 기능을 오프한다.")
    @Test
    void modifyIsPokable() {
        //given
        Coach coach = getCoachJason();

        //when
        Boolean expectedIsPokable = false;
        coach.modifyProfile(coach.getDescription(), expectedIsPokable);

        //then
        assertThat(coach.getIsPokable()).isEqualTo(expectedIsPokable);
    }
}
