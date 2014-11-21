package ru.savvy.jpafilterbuilder;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FilterConverterHelperTest {

    private Map<String, String> asserts = new HashMap<>();

    @Before
    public void fillInFormats(){
        asserts.put("2015-01-01", "yyyy-MM-dd");
        asserts.put("2015.01.01", "yyyy.MM.dd");
        asserts.put("2015/01/01", "yyyy/MM/dd");
        asserts.put("01/01/2015", "dd/MM/yyyy");
        asserts.put("01-01-2015", "dd-MM-yyyy");
        asserts.put("01.01.2015", "dd.MM.yyyy");
        asserts.put("2015-01-01    12:00 ", "yyyy-MM-dd HH:mm");
        asserts.put("2015.01.01 10:00",  "yyyy.MM.dd HH:mm");
        asserts.put("2015/01/01  11:00 ", "yyyy/MM/dd HH:mm");
        asserts.put("01/01/2015 11:11", "dd/MM/yyyy HH:mm");
        asserts.put("01-01-2015 14:05", "dd-MM-yyyy HH:mm");
        asserts.put("01.01.2015   12:14", "dd.MM.yyyy HH:mm");
    }


    /**
     * There is no attempt to test SimpleDateFormat here
     * it just expects that format matches the string given and tries to do its best to convert
     * hence "2015-01-01" with format "d-M-y" throws no format exception and gives wrong result Jul 08 2006, surprise
     */
    @Test
    public void dateConvertTest() throws Exception {
        for (Map.Entry<String, String> entry : asserts.entrySet()){
            Date expected = new SimpleDateFormat(entry.getValue()).parse(entry.getKey());
            assertEquals(expected, FilterConverterHelper.dateConvert(entry.getKey()));
        }
    }

}