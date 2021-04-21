package cn.eniaum.learn.satis;

import cn.eniaum.learn.satis.annotation.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 * @author Enaium
 */
@SuppressWarnings({"unchecked", "SuspiciousInvocationHandlerImplementation"})
public class Satis {

    private final Statement statement;

    public Satis(Map<String, String> config) throws Exception {
        Class.forName(config.get("driver"));
        Connection connection = DriverManager.getConnection(config.get("url"), config.get("username"), config.get("password"));
        statement = connection.createStatement();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    public <T> T getMapper(Class<?> mapper) {
        Object instance = Proxy.newProxyInstance(mapper.getClassLoader(), new Class<?>[]{mapper}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.isAnnotationPresent(SQL.class)) {
                    SQL sql = method.getAnnotation(SQL.class);
                    for (String value : sql.value()) {
                        if (isSelect(method) || isDelete(method)) {
                            int index = 0;
                            for (Parameter parameter : method.getParameters()) {
                                if (parameter.isAnnotationPresent(Param.class)) {
                                    Param param = parameter.getAnnotation(Param.class);
                                    value = value.replace("#{" + param.value() + "}", args[index].toString());
                                }
                                index++;
                            }
                        }

                        if (isInsert(method) || isUpdate(method)) {
                            Class<?> parameterType = method.getParameterTypes()[0];
                            for (Field declaredField : parameterType.getDeclaredFields()) {
                                value = value.replace("#{" + declaredField.getName() + "}", "\"" + parameterType.getMethod(getGetMethodName(declaredField.getName())).invoke(args[0]).toString() + "\"");
                            }
                        }
                        String typeName = method.getGenericReturnType().getTypeName();
                        ResultSet resultSet = statement.executeQuery(value);
                        if (!typeName.equals("void")) {
                            return toEntity(resultSet, typeName);
                        }
                    }
                }
                return null;
            }

            private boolean isSelect(Method method) {
                return method.isAnnotationPresent(Select.class);
            }

            private boolean isDelete(Method method) {
                return method.isAnnotationPresent(Delete.class);
            }

            private boolean isInsert(Method method) {
                return method.isAnnotationPresent(Insert.class);
            }

            private boolean isUpdate(Method method) {
                return method.isAnnotationPresent(Update.class);
            }
        });

        return (T) instance;
    }

    private <T> T toEntity(ResultSet resultSet, String className) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        boolean list = className.contains("<");
        if (list) {
            className = className.substring(className.indexOf("<") + 1, className.lastIndexOf(">"));
        }

        Class<?> klass = Class.forName(className);

        HashMap<String, Class<?>> fieldNameList = new HashMap<>();

        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
            fieldNameList.put(resultSet.getMetaData().getColumnName(i + 1), Class.forName(resultSet.getMetaData().getColumnClassName(i + 1)));
        }

        List<Object> objectList = new ArrayList<>();

        while (resultSet.next()) {
            Object instance = klass.newInstance();
            for (Map.Entry<String, Class<?>> entry : fieldNameList.entrySet()) {
                klass.getMethod(getSetMethodName(entry.getKey()), entry.getValue()).invoke(instance, resultSet.getObject(entry.getKey(), entry.getValue()));
            }
            objectList.add(instance);
        }

        resultSet.close();

        if (objectList.isEmpty()) {
            return null;
        }

        return list ? (T) objectList : (T) objectList.get(0);
    }

    private String getSetMethodName(String name) {
        return "set" + name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    private String getGetMethodName(String name) {
        return "get" + name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }
}