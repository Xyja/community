package com.newcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.tree.TreeNode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Pipe;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yongjiu, X
 * @create 2022-07-15 20:57
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "吗，";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        //类加载器就是从类路径下去加载资源 类路径指的是 target/classes/**
        //因为项目已启动，代码就被编译，文件就在类路径下都存在 所以我们可以在类路径下去读取资源
        try {
            //字节流
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            //把字节流转换成字符流  字节流读取不太方便？
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String keyword;
            while ((keyword = reader.readLine()) != null){
                //添加前缀树
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            //将error输出到文件
            logger.error("加载敏感词文件失败！" + e.getMessage());
        } finally {

        }

    }

    //将敏感词添加到前缀树
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++){
            Character c = keyword.charAt(i);

            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //移动当前节点和子节点的为止
            tempNode = subNode;

            //设置结束标识
            if (i == keyword.length()-1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词 并替换字符串的方法
     * @param text
     * @return
     */
    public String replaceSensitiveWords(String text){
        //处理空值
        if (StringUtils.isBlank(text)){
            return null;
        }

        //下面开始检索的操作 需要三个指针 一个指针默认指向前缀树rootNode
        //第二个指针遍历text 第三个指针来回对比 并进行标识敏感词长度的作用

        //指针一
        TrieNode tempNode = rootNode;

        //指针二、三
        int begin = 0;
        int position = 0;

        //替换后的返回结果
        StringBuilder sb = new StringBuilder();
        while (position < text.length()){
            char c = text.charAt(position);
            //跳过符号 避免敏感词中间夹杂符号却拦截不到
            if (isSymbol(c)){
                //若指针一处于根节点，将此符号计入结果，指针二向下走一步
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论什么情况，指针三都向下走一步
                position++;
                continue;
            }
            //到这里确定第一个不是符号 检查下一个节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个字符
                position = ++begin;
                //指针一重新指向根节点
                tempNode = rootNode;
            }else if (tempNode.isKeyWordEnd()){
                //发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一位置
                begin = ++position;
                //指针一重新指向根节点
                tempNode = rootNode;
            }else {
                //检查下一个字符
                position++;
            }
        }

        //最后一批字符计入结果
        sb.append(text.substring(begin,position));

        return sb.toString();

    }

    //跳过符号
    private boolean isSymbol(Character c){
        //(c < 0x2E80 || c > 0x9FFF) 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }



    //定义前缀树的结构  可以单独写一个类 也可以定义成内部类

    private class TrieNode {

        //关键词结束的标识
        private boolean isKeyWordEnd = false;

        //子节点 key是下级字符  value是下级节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
