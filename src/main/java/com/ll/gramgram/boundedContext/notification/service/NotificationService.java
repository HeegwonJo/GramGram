package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;

    //EventAfterLike
    @Transactional
    public void likeNotification(LikeablePerson likeablePerson) {
        Notification notification = Notification
                .builder()
                .fromInstaMember(likeablePerson.getFromInstaMember())
                .toInstaMember(likeablePerson.getToInstaMember())
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .newGender(likeablePerson.getFromInstaMember().getGender())
                .typeCode("Liked")
                .build();

        notificationRepository.save(notification);
    }

    //수정 알림
    @Transactional
    public void modifyNotification(LikeablePerson likeablePerson, int oldAttractiveTypeCode) {
        Notification notification = Notification
                .builder()
                .fromInstaMember(likeablePerson.getFromInstaMember())
                .toInstaMember(likeablePerson.getToInstaMember())
                .oldAttractiveTypeCode(oldAttractiveTypeCode)
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .typeCode("Modified")
                .build();

        notificationRepository.save(notification);
    }

    //삭제 알림
    @Transactional
    public void cancelNotification(LikeablePerson likeablePerson) {
        Notification notification = Notification
                .builder()
                .fromInstaMember(likeablePerson.getFromInstaMember())
                .toInstaMember(likeablePerson.getToInstaMember())
                .typeCode("Canceled")
                .build();
        notificationRepository.save(notification);
    }

    //성별 변경 알림
    @Transactional
    public void genderChangeNotification(InstaMember instaMember, String oldGender) {
        Notification notification = Notification
                .builder()
                .newGender(instaMember.getGender())
                .oldGender(oldGender)
                .fromInstaMember(instaMember)
                .typeCode("genderChanged")
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        return notificationRepository.findByToInstaMember(toInstaMember);
    }
}
