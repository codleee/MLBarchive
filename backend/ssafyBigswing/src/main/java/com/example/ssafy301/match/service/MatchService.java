package com.example.ssafy301.match.service;

import com.example.ssafy301.common.api.exception.NotFoundException;
import com.example.ssafy301.common.api.status.FailCode;
import com.example.ssafy301.match.domain.Match;
import com.example.ssafy301.match.domain.MatchDetail;
import com.example.ssafy301.match.dto.*;
import com.example.ssafy301.match.repository.MatchDetailRepository;
import com.example.ssafy301.match.repository.MatchRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.example.ssafy301.match.domain.QMatch.match;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchDetailRepository matchDetailRepository;
    private final JPAQueryFactory queryFactory;
    private final ObjectMapper objectMapper;
    
    // 오늘 있는 경기 목록 조회
    // 시간 순으로 정렬하기
    public List<MatchDto> getTodayMatches() {
        LocalDate now = LocalDate.now();
        
//        // 경기 목록을 전부 가져오기
//        List<Match> AllMatches = matchRepository.findAll();
//
//        // 우선 오늘 있는 경기만 분류할 것
//        List<Match> currentMatches = AllMatches.stream()
//                .filter(match ->
//                    match.getMatchDate().toLocalDate().isEqual(now)
//                ).collect(Collectors.toList());

        // 오늘 있는 매치만 가져오자
        List<MatchDto> currentMatches = queryFactory
                .select(new QMatchDto(
                        match
                ))
                .from(match)
                .where(matchDateEq(now))
                .orderBy(match.matchDate.asc())
                .fetch();


        // 경기가 없으면 예외 발생
        if(currentMatches == null || currentMatches.size() == 0) {
            throw new NotFoundException(FailCode.NO_MATCHES);
        }

        // 시간 순으로 정렬할 것
//        Collections.sort(currentMatches, (match1, match2) -> match1.getMatchDate().compareTo(match2.getMatchDate()));

        // 시간 순으로 정렬 됐으니
        // MatchDto로 변환하여 반환할것
        return currentMatches;
    }

    private BooleanExpression matchDateEq(LocalDate now) {
        return match.matchDate.year().eq(now.getYear())
                .and(match.matchDate.month().eq(now.getMonthValue()))
                .and(match.matchDate.dayOfMonth().eq(now.getDayOfMonth()));
    }

    // 경기 결과 상세 조회
    public MatchDetailDto getMatchById(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(() -> new NotFoundException(FailCode.NO_MATCH));
        return new MatchDetailDto(match);
    }

    // 경기 검색
    public Page<MatchDto> searchMatch(Pageable pageable, MatchSearchDto searchDto) {
        List<MatchDto> matchList = queryFactory
                .select(new QMatchDto(match))
                .from(match)
                .where(matchTeam(searchDto),
                        matchDateIn(searchDto))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 경기가 없으면 예외발생
        if(matchList == null || matchList.size() == 0) {
            throw new NotFoundException(FailCode.NO_MATCHES);
        }

        JPAQuery<Long> countQuery = queryFactory
                .select(match.count())
                .from(match)
                .where(matchTeam(searchDto),
                        matchDateIn(searchDto));

        return PageableExecutionUtils.getPage(matchList, pageable, countQuery::fetchOne);
    }

    private BooleanExpression matchDateIn(MatchSearchDto searchDto) {
        // 입력 기간 내의 경기목록
        return match.matchDate.between(searchDto.getStart().atStartOfDay(), searchDto.getEnd().atTime(LocalTime.MAX));
    }

    private BooleanExpression matchTeam(MatchSearchDto searchDto) {
        // 팀 이름 같은 것을 가져옴
        return match.homeId.eq(searchDto.getTeamId()).or(match.awayId.eq(searchDto.getTeamId()));
    }

    public MatchDetailJsonDto getDetailMatchByMatchId(Long matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new NotFoundException(FailCode.NO_MATCH));;
        MatchDetail matchDetail = matchDetailRepository.findById(match.getMatchDetailId()).orElseThrow(() -> new NotFoundException(FailCode.NO_MATCH));;;
        log.debug("DTO Result: " + matchDetail.getMatchId()+" zzzz ",matchDetail.getId());
        if (matchDetail == null) {
            throw new NotFoundException(FailCode.NO_MATCH);
        }

        try {
            log.debug("왜 안돼~~~~~~~~~~~~~~~~~~~~~~~~~~");
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MatchDetailJsonDto dto = objectMapper.readValue(matchDetail.getBoxscore(), MatchDetailJsonDto.class);
            log.debug("DTO Result: " + dto);
            return dto;
        } catch (IOException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }

    public LineScoreDto getLineScoreByMatchId(Long matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new NotFoundException(FailCode.NO_MATCH));;
        MatchDetail matchDetail = matchDetailRepository.findById(match.getMatchDetailId()).orElseThrow(() -> new NotFoundException(FailCode.NO_MATCH));;;
        if (matchDetail == null) {
            throw new NotFoundException(FailCode.NO_MATCH);
        }

        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(matchDetail.getLinescore(), LineScoreDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }
}
