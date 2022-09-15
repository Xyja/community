package com.newcoder.community.entity;

/** 封装分页信息的组件
 * @author Yongjiu, X
 * @create 2022-07-09 20:41
 */
public class PageInfo {

    // 当前页码 浏览器传过来
    private int currentPage = 1;  //默认是第一页

    //每页显示数据的条数  浏览器传过来
    private int limit =10; //默认是10条

    //数据的总记录数  服务端查询出来  用于计算总页数
    private int rows;

    //查询路径  作用是让页面上复用第几页这个变量  因为第一次页面传过来 服务端就记录好
    private String path;

    //对应的get、set方法 供外界使用

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        //需要做一些简单的判断  避免出现bug
        if(currentPage >= 1){

            this.currentPage = currentPage;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        //如果一页的数据太多 可能导致服务器变慢  反馈给浏览器也会变慢 成千上万
        if (limit >= 1 && limit <= 100){

            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0){

            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页起始行
     * @return
     */
    public int getOffset(){
        return (currentPage - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal(){

        if(rows % limit == 0){ //查出来的数据条数刚好是limit的倍数 就不用多分一页
            return rows / limit;
        }else {
            return rows / limit + 1;
        }
    }

    //页面需要显示以当前页为中心的前后四页 类似于页面导航  我需要算一下这四页的起始和末尾页码

    /**
     * 上一页的页码
     * @return
     */
    public int getFrom(){
        //限制页码不会超过首页
        int previousPage = currentPage - 2 < 1 ? 1 : currentPage - 2;
        return previousPage;
    }

    /**
     * 下一页的页码
     * @return
     */
    public int getTo(){

        //限制页码不会超过尾页
        int total = getTotal();
        int LastPage = currentPage + 2 > total ? total : currentPage + 2;
        return LastPage;
    }

}
