package com.woowacourse.teatime.teatime.service;

import com.woowacourse.teatime.teatime.controller.dto.request.CoachSaveRequest;
import com.woowacourse.teatime.teatime.controller.dto.request.CoachUpdateProfileRequest;
import com.woowacourse.teatime.teatime.controller.dto.response.CoachFindResponse;
import com.woowacourse.teatime.teatime.controller.dto.response.CoachProfileResponse;
import com.woowacourse.teatime.teatime.domain.Coach;
import com.woowacourse.teatime.teatime.domain.Crew;
import com.woowacourse.teatime.teatime.exception.NotFoundCoachException;
import com.woowacourse.teatime.teatime.exception.NotFoundCrewException;
import com.woowacourse.teatime.teatime.repository.CoachRepository;
import com.woowacourse.teatime.teatime.repository.CrewRepository;
import com.woowacourse.teatime.teatime.repository.dto.CoachWithPossible;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class CoachService {

    private final CoachRepository coachRepository;
    private final CrewRepository crewRepository;

    @Transactional(readOnly = true)
    public List<CoachFindResponse> findAll() {
        List<CoachWithPossible> coachWithPossibles = coachRepository.findCoaches();
        return CoachFindResponse.of(coachWithPossibles);
    }

    @Transactional(readOnly = true)
    public List<CoachFindResponse> findByName(Long crewId) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(NotFoundCrewException::new);
        List<CoachWithPossible> coachMeWithPossible = coachRepository.findByNameWithPossible(crew.getName());
        coachMeWithPossible.addAll(coachRepository.findCoaches());
        return CoachFindResponse.of(coachMeWithPossible);
    }

    public Long save(CoachSaveRequest request) {
        Coach coach = new Coach(request.getSlackId(),
                request.getName(),
                request.getEmail(),
                request.getImage());
        return coachRepository.save(coach).getId();
    }

    public void updateProfile(Long coachId, CoachUpdateProfileRequest request) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(NotFoundCoachException::new);
        coach.modifyProfile(request.getDescription(), request.getIsPokable());
    }

    public CoachProfileResponse getProfile(Long coachId) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(NotFoundCoachException::new);
        return new CoachProfileResponse(coach);
    }
}
