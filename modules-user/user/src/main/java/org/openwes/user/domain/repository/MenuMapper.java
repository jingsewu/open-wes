package org.openwes.user.domain.repository;

import org.openwes.user.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuMapper extends JpaRepository<Menu, Long> {
    List<Menu> findByTypeInAndEnableOrderByOrderNum(List<Integer> types, Integer enable);

    List<Menu> findByParentId(Long parentId);

    List<Menu> findAllByIdInOrderByOrderNum(List<Long> ids);
}
