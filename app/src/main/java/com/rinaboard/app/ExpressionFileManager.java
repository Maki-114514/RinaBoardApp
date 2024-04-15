package com.rinaboard.app;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExpressionFileManager {

    private static final String FILENAME = "expression.json";

    public static void createExpressionJsonFile(Context context) {
        try {
            context.openFileInput(FILENAME).close();
        } catch (FileNotFoundException e) {
            // 如果文件不存在，则创建一个新的 expression.json 文件，并写入基本的json格式
            try {
                FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write("{}".getBytes());
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将键值对添加到 expression.json 文件中
    public static void addKeyValuePair(Context context, String key, byte[] value) {
        try {
            // 读取已有的 JSON 数据
            JSONObject jsonObject = readJsonFromFile(context);

            if (jsonObject == null) {
                createExpressionJsonFile(context);
            } else {
                if (jsonObject.containsKey(key)) {
                    removeKey(context, key);
                }
                // 将键值对添加到 JSON 对象中
                jsonObject.put(key, value);

                // 将更新后的 JSON 对象写入文件
                writeJsonToFile(context, jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 从 expression.json 文件中删除特定的键
    public static void removeKey(Context context, String key) {
        try {
            // 读取原文件内容
            FileInputStream fis = context.openFileInput(FILENAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();

            // 解析 JSON 内容并移除指定的键
            String originalContent = stringBuilder.toString();
            JSONObject originalJson = JSON.parseObject(originalContent);
            originalJson.remove(key);

            // 将更新后的 JSON 对象写入文件
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(originalJson.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 从 Map 中获取所有的键，存储到字符串数组中
    public static String[] getAllKeysFromMap(Map<String, byte[]> map) {
        if (map != null) {
            Set<String> keySet = map.keySet();
            return keySet.toArray(new String[0]);
        } else {
            return null;
        }
    }

    // 从 JSONObject 中提取键值对，并存储到 HashMap 中
    public static Map<String, byte[]> getMapFromJson(Context context) {
        JSONObject jsonObject = readJsonFromFile(context);
        if (jsonObject != null) {
            Map<String, byte[]> map = new HashMap<>();
            for (String key : jsonObject.keySet()) {
                byte[] value = jsonObject.getBytes(key);
                map.put(key, value);
            }
            return map;
        } else {
            return null;
        }
    }

    // 从 expression.json 文件中读取 JSON 数据
    private static JSONObject readJsonFromFile(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonString = stringBuilder.toString();
        if (jsonString.isEmpty()) {
            return null; // 文件为空，返回 null
        } else {
            return JSON.parseObject(jsonString);
        }
    }

    // 将 JSON 对象写入 expression.json 文件
    private static void writeJsonToFile(Context context, JSONObject jsonObject) {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(jsonObject.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
