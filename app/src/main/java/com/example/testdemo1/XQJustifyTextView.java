package com.example.testdemo1;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
 */
public class XQJustifyTextView extends TextView {

    private static final String TAG = XQJustifyTextView.class.getSimpleName();

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
    private ArrayList<List<List<String>>> mParagraphLineList;

    /**
     * 当前所有的行数
     */
    private int mLineCount;

    /**
     * 每一段单词的内容集合
     */
    private ArrayList<List<String>> mParagraphWordList;

    /**
     * 空格字符
     */
    private static final String BLANK = " ";

    public XQJustifyTextView(Context context) {
        this(context, null);
    }

    public XQJustifyTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XQJustifyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        if (layout == null) {
            return;
        }
        adjust(canvas, paint);
    }

    /**
     * @param frontList
     * @return 计算每一段绘制的内容
     */
    private synchronized List<List<String>> getLineList(List<String> frontList) {
        Log.i(TAG, "getLineList ");
        StringBuilder sb = new StringBuilder();
        List<List<String>> lineLists = new ArrayList<>();
        List<String> lineList = new ArrayList<>();
        float width = 0;
        String temp = "";
        String front = "";
        for (int i = 0; i < frontList.size(); i++) {

            front = frontList.get(i);

            if (!TextUtils.isEmpty(temp)) {
                sb.append(temp);
                lineList.add(temp);
                if (!isCN(temp)) {
                    sb.append(BLANK);
                }
                temp = "";
            }

            if (isCN(front)) {
                sb.append(front);
            } else {
                if ((i + 1) < frontList.size()) {
                    String nextFront = frontList.get(i + 1);
                    if (isCN(nextFront)) {
                        sb.append(front);
                    } else {
                        sb.append(front).append(BLANK);
                    }
                } else {
                    sb.append(front);
                }

            }

            lineList.add(front);
            width = StaticLayout.getDesiredWidth(sb.toString(), getPaint());

            if (width > mViewWidth) {

                // 先判断最后一个单词是否是英文的，是的话则切割，否则的话就移除最后一个
                int lastIndex = lineList.size() - 1;
                String lastWord = lineList.get(lastIndex);

                String lastTemp = "";
                lineList.remove(lastIndex);
                if (isCN(lastWord)) {

                    addLines(lineLists, lineList);
                    lastTemp = lastWord;
                } else {

                    // 否则的话则截取字符串
                    String substring = sb.substring(0, sb.length() - lastWord.length() - 1);
                    sb.delete(0, sb.toString().length());
                    sb.append(substring);
                    String tempLastWord = "";

                    int length = lastWord.length();

                    if (length <= 3) {
                        addLines(lineLists, lineList);
                        lastTemp = lastWord;
                    } else {
                        for (int j = 0; j < length; j++) {

                            tempLastWord = String.valueOf(lastWord.charAt(j));
                            sb.append(tempLastWord);
                            width = StaticLayout.getDesiredWidth(sb.toString(), getPaint());

                            if (width > mViewWidth) {
                                if (j > 2) {
                                    // 单词截断后，前面的字符小于2个时，则不进行截断
                                    String lastFinalWord = lastWord.substring(0, j) + "-";
                                    lineList.add(lastFinalWord);
                                    addLines(lineLists, lineList);
                                    lastTemp = lastWord.substring(j, length);

                                } else {
                                    addLines(lineLists, lineList);
                                    lastTemp = lastWord;
                                }
                                break;
                            }
                        }
                    }
                }

                sb.delete(0, sb.toString().length());
                temp = lastTemp;

            }

            if (lineList.size() > 0 && i == frontList.size() - 1) {
                addLines(lineLists, lineList);
            }
        }

        if (!TextUtils.isEmpty(temp)) {
            lineList.add(temp);
            addLines(lineLists, lineList);
        }

        mLineCount += lineLists.size();
        return lineLists;
    }

    /**
     * 添加一行到单词内容
     *
     * @param lineLists 总单词集合
     * @param lineList 当前要添加的集合
     */
    private void addLines(List<List<String>> lineLists, List<String> lineList) {
        if (lineLists == null || lineList == null) {
            return;
        }

        List<String> tempLines = new ArrayList<>(lineList);
        lineLists.add(tempLines);
        lineList.clear();
    }

    /**
     * 获取段落
     */
    private void getParagraphList() {
        String text = getText().toString().replaceAll("  ", "").replaceAll("   ", "").replaceAll("\\r", "").trim();
        mLineCount = 0;
        String[] items = text.split("\\n");
        mParagraphLineList = new ArrayList<>();
        mParagraphWordList = new ArrayList<>();
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
        Log.i(TAG, "getWordList ");
        List<String> frontList = new ArrayList<>();
        StringBuilder enStr = new StringBuilder();
        StringBuilder cnStr = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char charAt = text.charAt(i);
            if (charAt != ' ') {
                if (!isCN(String.valueOf(charAt))) { // 英文
                    if (cnStr.length() != 0) {
                        // 添加中文到集合里去
                        frontList.add(cnStr.toString());
                        cnStr.delete(0, cnStr.length());
                    }
                    enStr.append(charAt);
                } else { // 中文

                    if (isPunctuation(charAt)) {
                        cnStr.append(charAt);
                    } else {
                        if (enStr.length() != 0) {
                            // 判断前一个字符串是英文时，添加到集合里去
                            frontList.add(enStr.toString());
                            enStr.delete(0, enStr.length());
                        }

                        if (cnStr.length() != 0) {
                            // 添加中文到集合里去
                            frontList.add(cnStr.toString());
                            cnStr.delete(0, cnStr.length());
                            cnStr.append(charAt);
                        } else {
                            cnStr.append(charAt);
                        }
                    }
                }
            } else {
                if (!TextUtils.isEmpty(enStr.toString())) {
                    frontList.add(enStr.toString().replaceAll(BLANK, ""));
                    enStr.delete(0, enStr.length());
                }
            }
        }

        if (enStr.length() != 0) {
            frontList.add(enStr.toString().replaceAll(BLANK, ""));
            enStr.delete(0, enStr.length());
        }

        if (cnStr.length() != 0) {
            frontList.add(cnStr.toString());
            cnStr.delete(0, cnStr.length());
        }

        return frontList;
    }


    /**
     * 中英文排版效果
     *
     * @param canvas
     */
    private synchronized void adjust(Canvas canvas, TextPaint paint) {

        int size = mParagraphWordList.size();

        for (int j = 0; j < size; j++) { // 遍历每一段
            List<List<String>> lineList = mParagraphLineList.get(j);
            for (int i = 0; i < lineList.size(); i++) { // 遍历每一段的每一行
                List<String> lineWords = lineList.get(i);
                if (i == lineList.size() - 1) {
                    drawScaledEndText(canvas, lineWords, paint);
                } else {
                    drawScaledText(canvas, lineWords, paint);
                }
                mLineY += getLineHeight();
            }
            mLineY += paragraphSpacing;
        }
    }

    /**
     * 绘制最后一行文字
     *  @param canvas
     * @param lineWords
     * @param paint
     */
    private void drawScaledEndText(Canvas canvas, List<String> lineWords, TextPaint paint) {
        if (canvas == null || lineWords == null || paint == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String aSplit : lineWords) {
            if (isCN(aSplit)) {
                sb.append(aSplit);
            } else {
                sb.append(aSplit).append(BLANK);
            }
        }
        canvas.drawText(sb.toString(), 0, mLineY, paint);
    }

    /**
     * 绘制左右对齐效果
     *  @param canvas
     * @param line
     * @param paint
     */
    private void drawScaledText(Canvas canvas, List<String> line, TextPaint paint) {
        if (canvas == null || line == null || paint == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String aSplit : line) {
            sb.append(aSplit);
        }

        float lineWidth = StaticLayout.getDesiredWidth(sb, getPaint());
        float cw = 0;
        float d = (mViewWidth - lineWidth) / (line.size() - 1);
        for (String aSplit : line) {
            canvas.drawText(aSplit, cw, mLineY, getPaint());
            cw += StaticLayout.getDesiredWidth(aSplit + "", paint) + d;
        }
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
