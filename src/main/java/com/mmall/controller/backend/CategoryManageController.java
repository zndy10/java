package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 测试接口
     * @return
     */
    @RequestMapping(value = "test.do", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse<Integer> test() {
        return ServiceResponse.createBySuccess();
    }


    /**
     * 添加品类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServiceResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        // 判断是否登录
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }

        // 判断是否是管理员 service中操作
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 登录者为管理员
            // 添加品类
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限 ");
        }
    }

    /**
     * 更新品类名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("update_category_name.do")
    @ResponseBody
    public ServiceResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        // 判断是否登录
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }

        // 判断是否是管理员 service中操作
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 登录者为管理员
            // 修改品类名称
             return iCategoryService.updateCategoryName(categoryId, categoryName);
        } else {
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限 ");
        }
    }

    /**
     * 获取当前分类的子分类
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_category.do", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        // 判断是否登录
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }

        // 判断是否是管理员 service中操作
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 登录者为管理员
            // 获取子分类，不递归
            return iCategoryService.getChildrenParallelCategory(categoryId);
        } else {
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限 ");
        }

    }

    /**
     * 递归获取子节点
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServiceResponse getCategoryAndChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        // 判断是否登录
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，请先登录");
        }

        // 判断是否是管理员 service中操作
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 登录者为管理员
            // 查询子分类，递归
            return iCategoryService.getCategoryAndChildrenById(categoryId);

        } else {
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限 ");
        }

    }
}
