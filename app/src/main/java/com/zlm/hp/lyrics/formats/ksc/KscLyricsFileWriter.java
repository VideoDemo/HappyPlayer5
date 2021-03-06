package com.zlm.hp.lyrics.formats.ksc;

import com.zlm.hp.lyrics.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.utils.CharUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * @Description: ksc歌词保存器
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 16:45
 * @Throws:
 */
public class KscLyricsFileWriter extends LyricsFileWriter {

    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "karaoke.songname";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "karaoke.singer";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "karaoke.offset";
    /**
     * 歌词 字符串
     */
    public final static String LEGAL_LYRICS_LINE_PREFIX = "karaoke.add";

    /**
     * 歌词Tag
     */
    public final static String LEGAL_TAG_PREFIX = "karaoke.tag";

    public KscLyricsFileWriter() {
        // 设置编码
        setDefaultCharset(Charset.forName("GB2312"));
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("ksc");
    }

    @Override
    public String getSupportFileExt() {
        return "ksc";
    }

    @Override
    public boolean writer(LyricsInfo lyricsIfno, String lyricsFilePath) throws Exception {
        String lyricsContent = getLyricsContent(lyricsIfno);
        return saveLyricsFile(lyricsContent, lyricsFilePath);
    }

    @Override
    public String getLyricsContent(LyricsInfo lyricsIfno) throws Exception {
        String lyricsCom = "";
        // 先保存所有的标签数据
        Map<String, Object> tags = lyricsIfno.getLyricsTags();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object val = entry.getValue();
            if (entry.getKey().equals(LyricsTag.TAG_TITLE)) {
                lyricsCom += LEGAL_SONGNAME_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_ARTIST)) {
                lyricsCom += LEGAL_SINGERNAME_PREFIX;
            } else if (entry.getKey().equals(LyricsTag.TAG_OFFSET)) {
                lyricsCom += LEGAL_OFFSET_PREFIX;
            } else {
                lyricsCom += LEGAL_TAG_PREFIX;
                val = entry.getKey() + ":" + val;
            }
            lyricsCom += " := '" + val + "';\n";
        }
        // 每行歌词内容
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsIfno
                .getLyricsLineInfoTreeMap();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);

            lyricsCom += LEGAL_LYRICS_LINE_PREFIX + "('"
                    + TimeUtils.parseString(lyricsLineInfo.getStartTime())
                    + "',";// 添加开始时间
            lyricsCom += "'"
                    + TimeUtils.parseString(lyricsLineInfo.getEndTime()) + "',";// 添加结束时间

            // 获取歌词文本行
            String lyricsText = getLineLyrics(lyricsLineInfo.getLineLyrics());
            lyricsCom += "'" + lyricsText + "',";// 解析文本歌词

            // 添加每个歌词的时间
            String wordsDisIntervalText = "";
            int wordsDisInterval[] = lyricsLineInfo.getWordsDisInterval();
            for (int j = 0; j < wordsDisInterval.length; j++) {
                if (j == 0)
                    wordsDisIntervalText += wordsDisInterval[j] + "";
                else
                    wordsDisIntervalText += "," + wordsDisInterval[j] + "";
            }
            lyricsCom += "'" + wordsDisIntervalText + "');\n";
        }
        return lyricsCom;
    }

    /**
     * 获取当行歌词
     *
     * @param lrcComTxt 歌词文本
     * @return
     */
    private String getLineLyrics(String lrcComTxt) {
        String newLrc = "";
        Stack<String> lrcStack = new Stack<String>();
        String temp = "";
        for (int i = 0; i < lrcComTxt.length(); i++) {
            char c = lrcComTxt.charAt(i);
            if (CharUtils.isChinese(c) || CharUtils.isHangulSyllables(c)
                    || CharUtils.isHiragana(c)) {

                if (!temp.equals("")) {
                    lrcStack.push(temp);
                    temp = "";
                }

                lrcStack.push(String.valueOf(c));
            } else if (Character.isSpaceChar(c)) {
                if (!temp.equals("")) {
                    lrcStack.push(temp);
                    temp = "";
                }
                String tw = lrcStack.pop();
                if (tw != null) {
                    lrcStack.push("[" + tw + " " + "]");
                }
            } else {
                temp += String.valueOf(c);
            }
        }
        //
        if (!temp.equals("")) {
            lrcStack.push("[" + temp + "]");
            temp = "";
        }

        String[] lyricsWords = new String[lrcStack.size()];
        Iterator<String> it = lrcStack.iterator();
        int i = 0;
        while (it.hasNext()) {
            String com = it.next();
            String tempCom = "";
            for (int j = 0; j < com.length(); j++) {
                char reg = com.charAt(j);
                if (reg == '[')
                    continue;
                if (reg == ']')
                    continue;
                tempCom += reg;
            }
            lyricsWords[i++] = tempCom;
            newLrc += com;
        }
        return newLrc;
    }
}
