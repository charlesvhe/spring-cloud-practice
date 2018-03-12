package com.github.charlesvhe.springcloud.practice.provider.service;

import com.github.charlesvhe.springcloud.practice.core.vo.PageData;
import com.github.charlesvhe.springcloud.practice.core.vo.PageRequest;
import com.github.charlesvhe.springcloud.practice.core.vo.Response;
import com.github.charlesvhe.springcloud.practice.provider.ProviderApiAutoConfig;
import com.github.charlesvhe.springcloud.practice.provider.entity.Product;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(ProviderApiAutoConfig.PLACE_HOLD_SERVICE_NAME)
public interface ProductService {
    // 为了让spring mvc能够正确绑定变量
    public class Page extends PageRequest<Product> {
    }

    // v1版api 即将废弃 pb=public 对外暴露所有人可调用
    @ApiOperation("分页查询")
    @RequestMapping(value = "/v1/pb/product", method = RequestMethod.GET)
    @Deprecated
    List<Product> selectAll(@RequestParam("offset") Integer offset, @RequestParam("limit") Integer limit);

    // 替代v1版的新版api pb=public 对外暴露所有人可调用
    @ApiOperation(value = "带过滤条件和排序的复杂分页查询", notes = "filter.categoryId必填")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.COMPATIBLE_VERSION, required = true)
    @RequestMapping(value = "/{version}/pb/product/action/search", method = RequestMethod.POST)
    Response<PageData<Product, Product>> selectAll(@RequestBody Page page);

    @ApiOperation("带过滤条件和排序的分页查询")
    @RequestMapping(value = "/{version}/pb/product", method = RequestMethod.GET)
    // 当前版本新开发api 随微服务整体升级 pt=protected 受保护的网关token验证合法可调用
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.CURRENT_VERSION, required = true)
    Response<PageData<Product, Product>> selectAllGet(Page page);

    @ApiOperation("按id查询详情")
    // 版本兼容api 随微服务整体升级 pt=protected 受保护的网关token验证合法可调用
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.COMPATIBLE_VERSION, required = true)
    @RequestMapping(value = "/{version}/pt/product/{id}", method = RequestMethod.GET)
    Response<Product> selectById(@PathVariable("id") Long id);

    // 版本兼容api 随微服务整体升级 pt=protected 受保护的网关token验证合法可调用
    @ApiOperation("添加")
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.COMPATIBLE_VERSION, required = true)
    @RequestMapping(value = "/{version}/pt/product", method = RequestMethod.POST)
    Response<Product> insert(@RequestBody Product product);

    @ApiOperation("按id删除(软删除)")
    // 版本兼容api 随微服务整体升级 pv=private 私有的微服务内部可调用 不对外暴露
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.COMPATIBLE_VERSION, required = true)
    @RequestMapping(value = "/{version}/pv/product/{id}", method = RequestMethod.DELETE)
    Response<Product> delete(@PathVariable("id") Long id);

    @ApiOperation("更新详情")
    // 版本兼容api 随微服务整体升级 pt=protected 受保护的网关token验证合法可调用
    @ApiImplicitParam(name = "version", paramType = "path", allowableValues = ProviderApiAutoConfig.COMPATIBLE_VERSION, required = true)
    @RequestMapping(value = "/{version}/pt/product", method = RequestMethod.PUT)
    Response<Product> update(@RequestBody Product product);
}
