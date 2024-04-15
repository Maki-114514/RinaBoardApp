package com.rinaboard.app;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.io.*;
import java.util.*;

public class StartAnimeFileManager {

    private static final String FILE_NAME = "startAnime.json";

    public static void createStartAnimeJsonFile(Context context) {
        try {
            context.openFileInput(FILE_NAME).close();
        } catch (FileNotFoundException e) {
            try {
                FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                fos.write("{}".getBytes());
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveStartAnimeList(Context context, String listName, LinkedList<StartAnime> startAnimeLinkedList) {
        try {
            Map<String, LinkedList<Map<String, Object>>> existingData = loadAllStartAnimeLists(context);
            LinkedList<Map<String, Object>> serializedList = new LinkedList<>();

            for (StartAnime anime : startAnimeLinkedList) {
                Map<String, Object> serializedAnime = new HashMap<>();
                serializedAnime.put("type", anime.getClass().getSimpleName());
                serializedAnime.put("data", JSON.parseObject(JSON.toJSONString(anime), Map.class));
                serializedList.add(serializedAnime);
            }

            existingData.put(listName, serializedList);

            String jsonData = JSON.toJSONString(existingData);
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonData.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStartAnimeList(Context context, String listName){
        try {
            Map<String, LinkedList<Map<String, Object>>> existingData = loadAllStartAnimeLists(context);
            existingData.remove(listName);

            String jsonData = JSON.toJSONString(existingData);
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonData.getBytes());
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static List<String> getAllStartAnimeNames(Context context) {
        List<String> names = new ArrayList<>();

        Map<String, LinkedList<Map<String, Object>>> allData = loadAllStartAnimeLists(context);

        for (String listName : allData.keySet()) {
            if(!listName.isEmpty()){
                names.add(listName);
            }
        }

        return names;
    }

    public static LinkedList<StartAnime> loadStartAnimeList(Context context, String listName) {
        LinkedList<StartAnime> startAnimeList = new LinkedList<>();

        Map<String, LinkedList<Map<String, Object>>> allData = loadAllStartAnimeLists(context);
        LinkedList<Map<String, Object>> serializedList = allData.get(listName);

        if (serializedList != null) {
            for (Map<String, Object> serializedAnime : serializedList) {
                String type = (String) serializedAnime.get("type");
                Map<String, Object> data = (Map<String, Object>) serializedAnime.get("data");

                if ("Expression".equals(type)) {
                    startAnimeList.add(JSON.parseObject(JSON.toJSONString(data), Expression.class));
                } else if ("ExpressionOnBoard".equals(type)) {
                    startAnimeList.add(JSON.parseObject(JSON.toJSONString(data), ExpressionOnBoard.class));
                } else if ("DelayMicroSeconds".equals(type)) {
                    startAnimeList.add(JSON.parseObject(JSON.toJSONString(data), DelayMicroSeconds.class));
                } else if ("DelaySeconds".equals(type)) {
                    startAnimeList.add(JSON.parseObject(JSON.toJSONString(data), DelaySeconds.class));
                }
            }
        }

        return startAnimeList;
    }

    private static Map<String, LinkedList<Map<String, Object>>> loadAllStartAnimeLists(Context context) {
        Map<String, LinkedList<Map<String, Object>>> data = new HashMap<>();
        try {
            FileReader fileReader = new FileReader(context.getFilesDir() + "/" + FILE_NAME);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            fileReader.close();

            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                data = JSON.parseObject(stringBuilder.toString(), new TypeReference<Map<String, LinkedList<Map<String, Object>>>>() {
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
