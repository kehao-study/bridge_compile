package com.bridge.console.service.team;

import com.bridge.console.model.vo.TeamDefVO;

/**
 * @author Jay
 * @version v1.0
 * @description team相关的服务
 * @date 2019-01-24 17:48
 */
public interface TeamService {


    /**
     * 添加team
     *
     * @param teamDefVO {@link TeamDefVO}
     * @return {@link Boolean}
     */
    Boolean addTeam(TeamDefVO teamDefVO);


    /**
     * 编辑team信息
     *
     * @param teamDefVO {@link TeamDefVO}
     * @return {@link Boolean}
     */
    Boolean editTeam(TeamDefVO teamDefVO);


    /**
     * 删除team
     *
     * @param teamId   团队id
     * @param modifier 修改人
     * @return {@link Boolean}
     */
    Boolean deleteTeam(Integer teamId, Integer modifier);
}
