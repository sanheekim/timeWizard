package com.minibean.timewizard.model.biz;

import java.util.List;

import com.minibean.timewizard.model.dto.FriendDto;

public interface FriendBiz {

	// 나와 친구인 유저들 목록
	public List<FriendDto> selectListF(int user_no);

	// 나와 친구가 아닌 유저들 목록
	public List<FriendDto> selectListN(int user_no);

}