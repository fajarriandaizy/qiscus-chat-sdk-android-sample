package com.qiscus.mychatui.data.source.impl;

import com.qiscus.mychatui.data.model.User;
import com.qiscus.mychatui.data.source.ChatRoomRepository;
import com.qiscus.mychatui.util.Action;
import com.qiscus.mychatui.util.AvatarUtil;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : January 31, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    @Override
    public void getChatRooms(Action<List<QChatRoom>> onSuccess, Action<Throwable> onError) {
        Observable.from(QiscusCore.getDataStore().getChatRooms(100))
                .filter(chatRoom -> chatRoom.getLastMessage() != null)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);

        QiscusApi.getInstance()
                .getAllChatRooms(true, false, true, 1, 100)
                .flatMap(Observable::from)
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom))
                .filter(chatRoom -> chatRoom.getLastMessage().getId() != 0)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void createChatRoom(User user, Action<QChatRoom> onSuccess, Action<Throwable> onError) {
        QChatRoom savedChatRoom = QiscusCore.getDataStore().getChatRoom(user.getId());
        if (savedChatRoom != null) {
            onSuccess.call(savedChatRoom);
            return;
        }

        QiscusApi.getInstance()
                .chatUser(user.getId(), null)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void createGroupChatRoom(String name, List<User> members, Action<QChatRoom> onSuccess, Action<Throwable> onError) {
        List<String> ids = new ArrayList<>();
        for (User member : members) {
            ids.add(member.getId());
        }

        QiscusApi.getInstance()
                .createGroupChat(name, ids, AvatarUtil.generateAvatar(name), null)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void addParticipant(long roomId, List<User> members, Action<Void> onSuccess, Action<Throwable> onError) {
        List<String> ids = new ArrayList<>();
        for (User member : members) {
            ids.add(member.getId());
        }
        QiscusApi.getInstance().addParticipants(roomId, ids)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRoom -> onSuccess.call(null), onError::call);
    }

    @Override
    public void removeParticipant(long roomId, List<User> members, Action<Void> onSuccess, Action<Throwable> onError) {
        List<String> ids = new ArrayList<>();
        for (User member : members) {
            ids.add(member.getId());
        }
        QiscusApi.getInstance().addParticipants(roomId, ids)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRoom -> onSuccess.call(null), onError::call);
    }

}
