package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Page selectListByTypeid(Integer typeid, Integer current) {
        Page<Shop> page = new Page(current,5);
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shop::getTypeId,typeid );
        Page<Shop> shopPage = baseMapper.selectPage(page, queryWrapper);
        return shopPage;
    }

//    @Override
//    public Result getByIdAndCashe(Integer id) {
//        String cashetoken = CACHE_SHOP_KEY + id;
//        String JsonShop = redisTemplate.opsForValue().get(cashetoken);
//        if (StrUtil.isNotBlank(JsonShop)){
//            Shop shop = JSONUtil.toBean(JsonShop, Shop.class);
//            return Result.ok(shop);
//        }
//        Shop shop = getById(id);
//        if (shop==null){
//            return Result.fail("404");
//        }
//        redisTemplate.opsForValue().set(cashetoken,JSONUtil.toJsonStr(shop));
//        redisTemplate.expire(cashetoken,CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        return Result.ok(shop);
//    }

    //缓存穿透
//@Override
//public Result getByIdAndCashe(Integer id) {
//    String cashetoken = CACHE_SHOP_KEY + id;
//    Map<Object, Object> entries = redisTemplate.opsForHash().entries(cashetoken);
//    if (!entries.isEmpty()){
//        if (entries.containsKey("redisnull")){
//            return Result.fail("店铺不存在");
//        }
//        Shop shop = BeanUtil.fillBeanWithMap(entries, new Shop(), false);
//        return Result.ok(shop);
//    }
//    Shop shop = getById(id);
//    if (shop==null){
//        HashMap<Object, Object> hashMap = new HashMap<>();
//        hashMap.put("redisnull","");
//        redisTemplate.opsForHash().putAll(cashetoken,hashMap);
//        redisTemplate.expire(cashetoken,CACHE_NULL_TTL,TimeUnit.MINUTES);
//        return Result.fail("404");
//    }
//    Map<String, Object> map = BeanUtil.beanToMap(shop,new HashMap<>(),CopyOptions.create().setIgnoreNullValue(true).
//            setFieldValueEditor((fieldname,filedvalue)->{
//               if (filedvalue==null){
//                   return null;
//               }
//                return  filedvalue.toString();
//            }));
//
//    redisTemplate.opsForHash().putAll(cashetoken,map);
//    redisTemplate.expire(cashetoken,CACHE_SHOP_TTL, TimeUnit.MINUTES);
//    return Result.ok(shop);
//}
    //缓存击穿互斥锁
    @Override
    public Result getByIdAndCashe(Integer id) {
        String cashetoken = CACHE_SHOP_KEY + id;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cashetoken);
        if (!entries.isEmpty()){
            if (entries.containsKey("redisnull")){
                return Result.fail("店铺不存在");
            }
            Shop shop = BeanUtil.fillBeanWithMap(entries, new Shop(), false);
            return Result.ok(shop);
        }
        Shop shop = null;
        try {
            while (!getLock(LOCK_SHOP_KEY + id)){
                Thread.sleep(40);
            }
            //双检查
            Map<Object, Object> entrietos = redisTemplate.opsForHash().entries(cashetoken);
            if (!entrietos.isEmpty()){
                if (entrietos.containsKey("__NULL__")){
                    return Result.fail("店铺不存在");
                }
                Shop shopto = BeanUtil.fillBeanWithMap(entrietos, new Shop(), false);
                return Result.ok(shopto);
            }
            shop = getById(id);
            if (shop==null){
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put("__NULL__","");
                redisTemplate.opsForHash().putAll(cashetoken,hashMap);
                redisTemplate.expire(cashetoken,CACHE_NULL_TTL,TimeUnit.MINUTES);
                return Result.fail("404");
            }
            Map<String, Object> map = BeanUtil.beanToMap(shop,new HashMap<>(),CopyOptions.create().setIgnoreNullValue(true).
                    setFieldValueEditor((fieldname,filedvalue)->{
                        if (filedvalue==null){
                            return null;
                        }
                        return  filedvalue.toString();
                    }));

            redisTemplate.opsForHash().putAll(cashetoken,map);
            redisTemplate.expire(cashetoken,CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            unlock(LOCK_SHOP_KEY+id);
        }
        return Result.ok(shop);
    }

    private boolean getLock(String key){
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.MILLISECONDS);
        return BooleanUtil.isTrue(aBoolean);
    }
    private void unlock(String key){
        redisTemplate.delete(key);
    }

    //缓存更新
    @Transactional
    @Override
    public Result updateAndDeteleCashe(Shop shop) {
            if (shop==null){
                return Result.fail("店铺id不能为空");
            }
       try {
           boolean bool = updateById(shop);

        if (bool==true){
             redisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        }}catch (Exception e){
           throw e;
       }
        return Result.ok();
    }
}
