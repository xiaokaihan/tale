package com.tale.ext;

import com.blade.jdbc.model.Paginator;
import com.blade.kit.*;
import com.tale.controller.BaseController;
import com.tale.dto.Comment;
import com.tale.dto.MetaDto;
import com.tale.dto.Types;
import com.tale.init.TaleConst;
import com.tale.model.Comments;
import com.tale.model.Contents;
import com.tale.service.SiteService;
import com.tale.utils.TaleUtils;
import com.vdurmont.emoji.EmojiParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主题公共函数
 * <p>
 * Created by biezhi on 2017/2/21.
 */
public final class Commons {

    private static SiteService siteService;

    private static final List EMPTY = new ArrayList(0);

    private static final Random rand = new Random();

    public static void setSiteService(SiteService ss) {
        siteService = ss;
    }

    /**
     * 判断分页中是否有数据
     *
     * @param paginator
     * @return
     */
    public static boolean is_empty(Paginator paginator) {
        return null == paginator || CollectionKit.isEmpty(paginator.getList());
    }

    /**
     * 网站链接
     *
     * @return
     */
    public static String site_url() {
        return site_url("");
    }

    /**
     * 返回网站链接下的全址
     *
     * @param sub 后面追加的地址
     * @return
     */
    public static String site_url(String sub) {
        return site_option("site_url") + sub;
    }

    /**
     * 网站标题
     *
     * @return
     */
    public static String site_title() {
        return site_option("site_title");
    }

    /**
     * 网站配置项
     *
     * @param key
     * @return
     */
    public static String site_option(String key) {
        return site_option(key, "");
    }

    /**
     * 网站配置项
     *
     * @param key
     * @param defalutValue 默认值
     * @return
     */
    public static String site_option(String key, String defalutValue) {
        if (StringKit.isBlank(key)) {
            return "";
        }
        return TaleConst.OPTIONS.get(key, defalutValue);
    }

    /**
     * 截取字符串
     *
     * @param str
     * @param len
     * @return
     */
    public static String substr(String str, int len) {
        if (str.length() > len) {
            return str.substring(0, len);
        }
        return str;
    }

    /**
     * 返回主题URL
     *
     * @return
     */
    public static String theme_url() {
        return site_url(BaseController.THEME);
    }

    /**
     * 返回主题下的文件路径
     *
     * @param sub
     * @return
     */
    public static String theme_url(String sub) {
        return site_url(BaseController.THEME + sub);
    }

    /**
     * 返回gravatar头像地址
     *
     * @param email
     * @return
     */
    public static String gravatar(String email) {
        String avatarUrl = "https://secure.gravatar.com/avatar";
        if (StringKit.isBlank(email)) {
            return avatarUrl;
        }
        String hash = Tools.md5(email.trim().toLowerCase());
        return avatarUrl + "/" + hash;
    }

    /**
     * 返回文章链接地址
     *
     * @param contents
     * @return
     */
    public static String permalink(Contents contents) {
        return permalink(contents.getCid(), contents.getSlug());
    }

    /**
     * 返回文章链接地址
     *
     * @param cid
     * @param slug
     * @return
     */
    public static String permalink(Integer cid, String slug) {
        return site_url("/article/" + (StringKit.isNotBlank(slug) ? slug : cid.toString()));
    }

    /**
     * 格式化unix时间戳为日期
     *
     * @param unixTime
     * @return
     */
    public static String fmtdate(Integer unixTime) {
        return fmtdate(unixTime, "yyyy-MM-dd");
    }

    /**
     * 格式化unix时间戳为日期
     *
     * @param unixTime
     * @param patten
     * @return
     */
    public static String fmtdate(Integer unixTime, String patten) {
        if (null != unixTime && StringKit.isNotBlank(patten)) {
            return DateKit.formatDateByUnixTime(unixTime, patten);
        }
        return "";
    }

    /**
     * 显示分类
     *
     * @param categories
     * @return
     */
    public static String show_categories(String categories) throws UnsupportedEncodingException {
        if (StringKit.isNotBlank(categories)) {
            String[] arr = categories.split(",");
            StringBuffer sbuf = new StringBuffer();
            for (String c : arr) {
                sbuf.append("<a href=\"/category/" + URLEncoder.encode(c, "UTF-8") + "\">" + c + "</a>");
            }
            return sbuf.toString();
        }
        return show_categories("默认分类");
    }

    /**
     * 显示标签
     *
     * @param tags
     * @return
     */
    public static String show_tags(String tags) throws UnsupportedEncodingException {
        if (StringKit.isNotBlank(tags)) {
            String[] arr = tags.split(",");
            StringBuffer sbuf = new StringBuffer();
            for (String c : arr) {
                sbuf.append("<a href=\"/tag/" + URLEncoder.encode(c, "UTF-8") + "\">" + c + "</a>");
            }
            return sbuf.toString();
        }
        return "";
    }

    /**
     * 截取文章摘要
     *
     * @param value 文章内容
     * @param len   要截取文字的个数
     * @return
     */
    public static String intro(String value, int len) {
        int pos = value.indexOf("<!--more-->");
        if (pos != -1) {
            String html = value.substring(0, pos);
            return TaleUtils.htmlToText(TaleUtils.mdToHtml(html));
        } else {
            String text = TaleUtils.htmlToText(TaleUtils.mdToHtml(value));
            if (text.length() > len) {
                return text.substring(0, len);
            }
            return text;
        }
    }

    /**
     * 显示文章内容，转换markdown为html
     *
     * @param value
     * @return
     */
    public static String article(String value) {
        if (StringKit.isNotBlank(value)) {
            value = value.replace("<!--more-->", "\r\n");
            return TaleUtils.mdToHtml(value);
        }
        return "";
    }

    /**
     * 显示文章缩略图，顺序为：文章第一张图 -> 随机获取
     *
     * @return
     */
    public static String show_thumb(Contents contents) {
        if (null == contents) {
            return "";
        }
        String content = article(contents.getContent());
        String img = show_thumb(content);
        if (StringKit.isNotBlank(img)) {
            return img;
        }
        int cid = contents.getCid();
        int size = cid % 20;
        size = size == 0 ? 1 : size;
        return "/static/user/img/rand/" + size + ".jpg";
    }

    /**
     * 获取随机数
     * @param max
     * @param str
     * @return
     */
    public static String random(int max, String str){
        return UUID.random(1, max) + str;
    }

    /**
     * 最新文章
     *
     * @param limit
     * @return
     */
    public static List<Contents> recent_articles(int limit) {
        if (null == siteService) {
            return EMPTY;
        }
        return siteService.recentContents(limit);
    }

    /**
     * 最新评论
     *
     * @param limit
     * @return
     */
    public static List<Comments> recent_comments(int limit) {
        if (null == siteService) {
            return EMPTY;
        }
        return siteService.recentComments(limit);
    }

    /**
     * 获取分类列表
     * @return
     */
    public static List<MetaDto> categries(int limit){
        return siteService.metas(Types.CATEGORY, null, limit);
    }

    /**
     * 获取所有分类
     * @return
     */
    public static List<MetaDto> categries(){
        return categries(TaleConst.MAX_POSTS);
    }

    /**
     * 获取标签列表
     * @return
     */
    public static List<MetaDto> tags(int limit){
        return siteService.metas(Types.TAG, null, limit);
    }

    /**
     * 获取所有标签
     * @return
     */
    public static List<MetaDto> tags(){
        return tags(TaleConst.MAX_POSTS);
    }

    /**
     * 获取评论at信息
     * @param coid
     * @return
     */
    public static String comment_at(Integer coid){
        Comments comments = siteService.getComment(coid);
        if(null != comments){
            return "<a href=\"#comment-" + coid + "\">@" + comments.getAuthor() + "</a>";
        }
        return "";
    }

    /**
     * An :grinning:awesome :smiley:string &#128516;with a few :wink:emojis!
     *
     * 这种格式的字符转换为emoji表情
     *
     * @param value
     * @return
     */
    public static String emoji(String value){
        return EmojiParser.parseToUnicode(value);
    }

    /**
     * 获取文章第一张图片
     *
     * @return
     */
    public static String show_thumb(String content) {
        content = TaleUtils.mdToHtml(content);
        if (content.contains("<img")) {
            String img = "";
            String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
            Pattern p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
            Matcher m_image = p_image.matcher(content);
            if (m_image.find()) {
                img = img + "," + m_image.group();
                // //匹配src
                Matcher m = Pattern.compile("src\\s*=\\s*\'?\"?(.*?)(\'|\"|>|\\s+)").matcher(img);
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        return "";
    }

    private static final String[] ICONS = {"bg-ico-book", "bg-ico-game", "bg-ico-note", "bg-ico-chat", "bg-ico-code", "bg-ico-image", "bg-ico-web", "bg-ico-link", "bg-ico-design", "bg-ico-lock"};

    /**
     * 显示文章图标
     *
     * @param cid
     * @return
     */
    public static String show_icon(int cid) {
        return ICONS[cid % ICONS.length];
    }

}
