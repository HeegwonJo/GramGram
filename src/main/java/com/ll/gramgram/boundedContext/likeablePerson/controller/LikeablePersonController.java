package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.base.baseEntity.QBaseEntity;
import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/usr/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/like")
    public String showLike() {
        return "usr/likeablePerson/like";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like")
    public String like(@Valid LikeForm likeForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.like(rq.getMember(), likeForm.getUsername(), likeForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String showList(Model model) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            // 해당 인스타회원이 좋아하는 사람들 목록
            List<LikeablePerson> likeablePeople = instaMember.getFromLikeablePeople();
            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/list";
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public String cancel(@PathVariable Long id) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElse(null);

        RsData canDeleteRsData = likeablePersonService.canCancel(rq.getMember(), likeablePerson);

        if (canDeleteRsData.isFail()) return rq.historyBack(canDeleteRsData);

        RsData deleteRsData = likeablePersonService.cancel(likeablePerson);

        if (deleteRsData.isFail()) return rq.historyBack(deleteRsData);

        return rq.redirectWithMsg("/usr/likeablePerson/list", deleteRsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String showModify(@PathVariable Long id, Model model) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElseThrow();

        RsData canModifyRsData = likeablePersonService.canModify(rq.getMember(), likeablePerson);

        if (canModifyRsData.isFail()) return rq.historyBack(canModifyRsData);

        model.addAttribute("likeablePerson", likeablePerson);

        return "usr/likeablePerson/modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@PathVariable Long id, @Valid ModifyForm modifyForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.modifyAttractive(rq.getMember(), id, modifyForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/toList")
    public String showToList(Model model,
                             @RequestParam(name = "gender", required = false) String gender,
                             @RequestParam(name = "attractiveTypeCode", required = false) Integer attractiveTypeCode,
                             @RequestParam(name = "sortCode", required = false) Integer sortCode) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            // 해당 인스타회원이 좋아하는 사람들 목록
            List<LikeablePerson> likeablePeople;
            if (gender == null || gender.isBlank()) { // gender가 선택되지 않은 경우, 빈칸인 경우
                likeablePeople = instaMember.getToLikeablePeople();
            } else { // gender가 선택된 경우
                likeablePeople = instaMember.getToLikeablePeople().stream()
                        .filter(likeablePerson -> likeablePerson.getFromInstaMember().getGender().equals(gender))
                        .collect(Collectors.toList());
            }
            if (attractiveTypeCode != null) { //호감 사유가 선택된 경우
                likeablePeople = likeablePeople.stream()
                        .filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == (attractiveTypeCode))
                        .collect(Collectors.toList());
            }

            if (sortCode != null) {
                switch (sortCode) {
                    case 2: //날짜순
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(BaseEntity::getCreateDate))
                                .collect(Collectors.toList());
                        break;
                    case 3:// 인기순
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(a -> a.getFromInstaMember().getToLikeablePeople().size(), Comparator.reverseOrder()))
                                .collect(Collectors.toList());
                        break;

                    case 4:// 인기 역순
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(a -> a.getFromInstaMember().getToLikeablePeople().size()))
                                .collect(Collectors.toList());
                        break;
                    case 5: // 성별순 여성 먼저.
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(a -> a.getFromInstaMember().getGender(),Comparator.reverseOrder()))
                                .sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed())
                                .collect(Collectors.toList());
                        break;
                    case 6: //호감 사유 순
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparingInt(LikeablePerson::getAttractiveTypeCode))
                                .sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed())
                                .collect(Collectors.toList());
                        break;
                    case 1: // 최신순 (기본값)
                    default:
                        likeablePeople = likeablePeople.stream()
                                .sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed())
                                .collect(Collectors.toList());

                }
            }

            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/toList";
    }

    @AllArgsConstructor
    @Getter
    public static class LikeForm {
        @NotBlank
        @Size(min = 3, max = 30)
        private final String username;
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @AllArgsConstructor
    @Getter
    public static class ModifyForm {
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

}
