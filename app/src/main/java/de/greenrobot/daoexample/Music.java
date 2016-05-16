package de.greenrobot.daoexample;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table MUSIC.
 */
public class Music {

    private Long uid;
    private String name;
    private String artist;
    private String cache_url;
    private String url;
    private String lrc_url;
    private String lrc_cache_url;
    private String pic_url;
    private Integer source;
    private Long parameter;

    public Music() {
    }

    public Music(Long uid) {
        this.uid = uid;
    }
    public Music(Music m){
    	this.uid = m.getUid();
        this.name = m.getName();
        this.artist = m.getArtist();
        this.cache_url = m.getCache_url();
        this.url = m.getUrl();
        this.lrc_url = m.getLrc_url();
        this.lrc_cache_url = m.getLrc_cache_url();
        this.pic_url = m.getPic_url();
        this.source = m.getSource();
        this.parameter = m.getParameter();
    }
    public Music(Long uid, String name, String artist, String cache_url, String url, String lrc_url, String lrc_cache_url, String pic_url, Integer source, Long parameter) {
        this.uid = uid;
        this.name = name;
        this.artist = artist;
        this.cache_url = cache_url;
        this.url = url;
        this.lrc_url = lrc_url;
        this.lrc_cache_url = lrc_cache_url;
        this.pic_url = pic_url;
        this.source = source;
        this.parameter = parameter;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCache_url() {
        return cache_url;
    }

    public void setCache_url(String cache_url) {
        this.cache_url = cache_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLrc_url() {
        return lrc_url;
    }

    public void setLrc_url(String lrc_url) {
        this.lrc_url = lrc_url;
    }

    public String getLrc_cache_url() {
        return lrc_cache_url;
    }

    public void setLrc_cache_url(String lrc_cache_url) {
        this.lrc_cache_url = lrc_cache_url;
    }

    public String getPic_url() {
        return pic_url;
    }

    public void setPic_url(String pic_url) {
        this.pic_url = pic_url;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Long getParameter() {
        return parameter;
    }

    public void setParameter(Long parameter) {
        this.parameter = parameter;
    }

}
