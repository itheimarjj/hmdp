package com.hmdp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IShopService extends IService<Shop> {

    Page selectListByTypeid(Integer typeid, Integer current);

    Result getByIdAndCashe(Integer id);

    Result updateAndDeteleCashe(Shop shop);
}
