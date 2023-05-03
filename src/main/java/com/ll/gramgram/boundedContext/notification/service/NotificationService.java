package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;

    //등록 알림
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
                .newGender(likeablePerson.getFromInstaMember().getGender())
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
                .newGender(likeablePerson.getFromInstaMember().getGender())
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

    @Transactional
    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        List<Notification> notifications = notificationRepository.findByToInstaMember(toInstaMember);
        //readDate 업데이트
        updateReadDate(notifications);

        //생성일시 역순으로 정렬
        notifications.sort(Comparator.comparing(Notification::getCreateDate).reversed());

        return notifications;
    }
    @Transactional
    public RsData updateReadDate(List<Notification> notifications){
        for (Notification notification: notifications){
            if (notification.getReadDate()==null){
                notification.updateReadDate();
                notificationRepository.save(notification);
            }
        }
        return RsData.of("S-1", "열람일시 수정 완료");
    }
}
