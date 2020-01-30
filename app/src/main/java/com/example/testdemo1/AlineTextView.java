package com.example.testdemo1;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: 自定义分散对齐的TextView(中英文排版效果)
 * 待优化待点：中英文混排时，展示空格过大；
 * <p>
 * <p>
 * 1，先截取字符串
 * 2，将字符串里的字符拆分出来
 * 3，通过计算剩余宽度进行绘制
 * <p>
 * <p>
 * 1,中文：截取文字，依次计算排列--- 无问题
 * 2，非中文：截取单词，依次计算排列----出现间隙过大问题， 解决方案，将单词截断，用-连接
 * 3，中和非中文混排：截取单词，依次计算排列----出现间隙过大问题， 解决方案，将单词截断，用-连接
 * <p>
 * 至此，进行改造；
 * <p>
 * 1,onmeasure, 将单词进行切割分配，
 * 2，ondraw,安装坐标进行绘制
 */
public class AlineTextView extends TextView {

    /**
     * 绘制文字的起始Y坐标
     */
    private float mLineY;

    /**
     * 文字的宽度
     */
    private int mViewWidth;

    /**
     * 段落间距
     */
    private int paragraphSpacing = dipToPx(getContext(), 15);

    /**
     * 当前所有行数的集合
     */
    private ArrayList<List<String>> mParagraphLineList;

    /**
     * 当前所有的行数
     */
    private int mLineCount;

    /**
     * 每一段单词的内容集合
     */
    private ArrayList<List<String>> mParagraphWordList;

    public AlineTextView(Context context) {
        this(context, null);
    }

    public AlineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mParagraphLineList = null;
        mParagraphWordList = null;
        mLineY = 0;
        mViewWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        getParagraphList();
        for (List<String> frontList : mParagraphWordList) {
            mParagraphLineList.add(getLineList(frontList));
        }
        setMeasuredDimension(mViewWidth, (mParagraphLineList.size() - 1) * paragraphSpacing + mLineCount * getLineHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mLineY = 0;
        float textSize = getTextSize();
        mLineY += textSize + getPaddingTop();
        Layout layout = getLayout();
        // layout.getLayout()在4.4.3出现NullPointerException
        if (layout == null) {
            return;
        }
        adjust(canvas, paint);
    }

    /**
     * @param frontList
     * @return 计算每一段绘制的内容
     */
    private synchronized List<String> getLineList(List<String> frontList) {
        StringBuilder sb = new StringBuilder();
        List<String> lineList = new ArrayList<>();
        float width = 0;
        int count = 0;
        String temp = null;
        String front = null;
        for (int i = 0; i < frontList.size(); i++) {
            front = frontList.get(i);
            if (temp != null) {
                sb.append(temp);
                if (!isPureCN(temp)) {
                    sb.append(" ");
                }
                temp = null;
                count++;
            }
            if (isPureCN(front)) {
                if (i != frontList.size() - 1) {
                    String newFront = frontList.get(i + 1);
                    if (!isPureCN(newFront)) {
                        sb.append(front + " ");
                    } else {
                        sb.append(front);
                    }
                } else {
                    sb.append(front);
                }
            } else {
                sb.append(front + " ");
            }
            count++;
            width = StaticLayout.getDesiredWidth(sb.toString(), getPaint());
            if (width > mViewWidth) {
                if (count <= 3) {
                    temp = spliteFront(sb, front, lineList);
                    sb.delete(0, sb.toString().length());
                    count = 0;
                } else {
                    String line = sb.toString();
                    if (isCN(line.charAt(line.length() - 1) + "")) {
                        lineList.add(sb.toString().substring(0, sb.toString().length() - front.length()));
                    } else {
                        lineList.add(sb.toString().substring(0, sb.toString().length() - front.length() - 1));
                    }
                    sb.delete(0, sb.toString().length());
                    temp = front;
                    count = 0;
                }
            }

            if (i == frontList.size() - 1) {
                if (temp != null) {
                    sb.append(temp + " ");
                    temp = null;
                }
                if (sb.toString().length() != 0) {
                    lineList.add(sb.toString());
                }
            }
        }

//        YLLog.i("TAG", "------>lineList:" + lineList.toString());
        mLineCount += lineList.size();
        return lineList;
    }

    /**
     * 容错处理（当一行的内容放不下三个时，进行切割处理）
     *
     * @param line     一行绘制的内容
     * @param front    最后一个单词
     * @param lineList
     * @return 切割后的单词
     */
    private String spliteFront(StringBuilder line, String front, List<String> lineList) {
        StringBuilder sb = new StringBuilder();
        sb.append(line.substring(0, line.toString().length() - front.length() - 1));
        String lastWord = null;
        String temp = null;
        float width = 0;
        for (int i = 0; i < front.length(); i++) {
            if (temp != null) {
                sb.append(temp);
                temp = null;
            }
            sb.append(front.charAt(i));
            width = StaticLayout.getDesiredWidth(sb.toString(), getPaint());
            if (width > mViewWidth) {
                lineList.add(sb.toString().substring(0, sb.toString().length() - 1));
                temp = front.charAt(i) + "";
                sb.delete(0, sb.toString().length());
            }

            if (i == front.length() - 1) {
                if (temp != null) {
                    sb.append(temp);
                    temp = null;
                }
                if (sb.toString().length() != 0) {
                    lastWord = sb.toString();
                    sb.delete(0, sb.toString().length());
                }
            }
        }
        return lastWord;
    }

    /**
     * 获取段落
     */
    private void getParagraphList() {
        String text = getText().toString().replaceAll("  ", "").replaceAll("   ", "").replaceAll("\\r", "").trim();
        mLineCount = 0;
        String[] items = text.split("\\n");
        mParagraphLineList = new ArrayList<List<String>>();
        mParagraphWordList = new ArrayList<List<String>>();
        for (String item : items) {
            if (item.length() != 0) {
                mParagraphWordList.add(getWordList(item));
            }
        }
    }

    /**
     * 截取每一段内容的每一个单词
     *
     * @param text
     * @return
     */
    private synchronized List<String> getWordList(String text) {
        List<String> frontList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        StringBuilder sbCN = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != ' ') {
                if (!isPureCN(text.charAt(i) + "")) { //英文
                    if (sbCN.toString().length() != 0) {
                        sb.append(sbCN.toString());
                        sbCN.delete(0, sbCN.toString().length());
                    }
                    sb.append(text.charAt(i));
                } else { //中文
                    if (isPunctuation(text.charAt(i))) {
                        sbCN.append(text.charAt(i));
                    } else {
                        if (sbCN.toString().length() != 0) {
                            if (sb.toString().length() != 0) {
                                sb.append(sbCN.toString());
                                frontList.add(sb.toString().replaceAll(" ", ""));
                                sb.delete(0, sb.toString().length());
                                sbCN.delete(0, sbCN.toString().length());
                                sbCN.append(text.charAt(i));
                            } else {
                                frontList.add(sbCN.toString());
                                sbCN.delete(0, sbCN.toString().length());
                                sbCN.append(text.charAt(i));
                            }
                        } else {
                            sbCN.append(text.charAt(i));
                        }
                    }
                }
            } else {
                if (!TextUtils.isEmpty(sb.toString())) {
                    if (sbCN.toString().length() != 0) {
                        sb.append(sbCN.toString());
                        sbCN.delete(0, sbCN.toString().length());
                    }
                    frontList.add(sb.toString().replaceAll(" ", ""));
                    sb.delete(0, sb.toString().length());
                }

                if (!TextUtils.isEmpty(sbCN.toString())) {
                    if (sbCN.toString().length() != 0) {
                        frontList.add(sbCN.toString().replaceAll(" ", ""));
                        sbCN.delete(0, sbCN.toString().length());
                    }
                }
            }
        }
//        YLLog.i("TAG", "------>frontList:" + frontList.toString());
        if (sbCN.toString().length() != 0) {
            sb.append(sbCN.toString());
            sbCN.delete(0, sbCN.toString().length());
        }
        if (sb.toString().length() != 0) {
            frontList.add(sb.toString().replaceAll(" ", ""));
            sb.delete(0, sb.toString().length());
        }

        return frontList;
    }


    /**
     * 中英文排版效果
     *
     * @param canvas
     * @param paint
     */
    private synchronized void adjust(Canvas canvas, TextPaint paint) {
        for (int j = 0; j < mParagraphLineList.size(); j++) {//遍历每一段
            for (int i = 0; i < mParagraphLineList.get(j).size(); i++) {//遍历每一段的每一行
                if (i == mParagraphLineList.get(j).size() - 1) {
                    canvas.drawText(mParagraphLineList.get(j).get(i), 0, mLineY, paint);
//                    YLLog.i("TAG", "------>line:" + mParagraphLineList.get(j).get(i));
                } else {
                    String line = mParagraphLineList.get(j).get(i);
                    if (needScale(line)) {
                        drawScaledEndLishText(canvas, line);
//                        YLLog.i("TAG", "------>line:" + line);
                    }
                }
//                YLLog.i("TAG", "------>line:" + mParagraphLineList.get(j).get(i));
                mLineY += getLineHeight();
            }
            mLineY += paragraphSpacing;
        }
    }

    /**
     * 绘制左右对齐效果
     *
     * @param canvas
     * @param line
     */
    private void drawScaledEndLishText(Canvas canvas, String line) {
        if (canvas == null || TextUtils.isEmpty(line)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<String> fronts = new ArrayList<>();

        boolean pureCN = isPureCN(line);
        boolean pureEN = isPureEN(line);
        boolean containChinese = isContainChinese(line);
        if (!pureCN && !containChinese) {
            String[] split = line.split(" ");
            for (String front : split) {
                fronts.add(front);
                sb.append(front);
            }
        } else {
//            line.replaceAll(" ", "").replaceAll("\\r", "");
            for (int i = 0; i < line.length(); i++) {
                String charAt = line.charAt(i) + "";
                String trim = charAt.trim();
                if (TextUtils.isEmpty(trim)) {
                    continue;
                }
                sb.append(trim);
                fronts.add(trim);
            }
        }

        float lineWidth = StaticLayout.getDesiredWidth(sb, getPaint());
        float cw = 0;
        float d = (mViewWidth - lineWidth) / (fronts.size() - 1);
        for (String aSplit : fronts) {
            canvas.drawText(aSplit + "", cw, mLineY, getPaint());
            cw += StaticLayout.getDesiredWidth(aSplit + "", getPaint()) + d;
        }
    }

    private boolean needScale(String line) {
        return !(line == null || line.length() == 0) && line.charAt(line.length() - 1) != '\n';
    }

    /**
     * 功能：判断字符串是否有中文
     *
     * @param str
     * @return
     */
    public boolean isCN(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8");
            if (bytes.length == str.length()) {
                return false;
            } else {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static int dipToPx(Context var0, float var1) {
        float var2 = var0.getResources().getDisplayMetrics().density;
        return (int) (var1 * var2 + 0.5F);
    }

    /**
     * 功能：判断字符串是否是纯中文
     *
     * @param str
     * @return
     */
    public boolean isPureCN(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8");
            if (bytes.length == str.length() * 3) { //纯中文
                return true;
            } else if (bytes.length < str.length() * 3) {//包含中文
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 功能：判断字符串是否是纯英文
     *
     * @param str
     * @return
     */
    public boolean isPureEN(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8");
            if (bytes.length == str.length()) { //纯英文
                return true;
            } else if (bytes.length < str.length() * 3) {//包含中文
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }


    /**
     * 判断是否包含标点符号等内容
     *
     * @param ch
     * @return
     */
    public boolean isPunctuation(char ch) {
        if (isCjkPunc(ch)) return true;
        if (isEnPunc(ch)) return true;

        if (0x2018 <= ch && ch <= 0x201F) return true;
        if (ch == 0xFF01 || ch == 0xFF02) return true;
        if (ch == 0xFF07 || ch == 0xFF0C) return true;
        if (ch == 0xFF1A || ch == 0xFF1B) return true;
        if (ch == 0xFF1F || ch == 0xFF61) return true;
        if (ch == 0xFF0E) return true;
        if (ch == 0xFF65) return true;

        return false;
    }

    private boolean isEnPunc(char ch) {
        if (0x21 <= ch && ch <= 0x22) return true;
        if (ch == 0x27 || ch == 0x2C) return true;
        if (ch == 0x2E || ch == 0x3A) return true;
        if (ch == 0x3B || ch == 0x3F) return true;

        return false;
    }

    private boolean isCjkPunc(char ch) {
        if (0x3001 <= ch && ch <= 0x3003) return true;
        if (0x301D <= ch && ch <= 0x301F) return true;

        return false;
    }
}
