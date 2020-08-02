package lz.demo.shop.web.router;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import lz.demo.shop.web.WebApiResponse;
import lz.demo.shop.web.action.Action;
import lz.demo.shop.web.annotation.RequestMapping;
import lz.demo.shop.web.label.HttpLabel;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
@Slf4j
public class HttpRouter extends ClassLoader{
    private Map<HttpLabel, Action<WebApiResponse>> httpRouterAction = new HashMap<>();

    private Map<String,Object> controllersBeans = new HashMap<>();

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String classpath = "";
        URL source = this.getClass().getResource("");
        if (source != null) {
            classpath = source.getPath();
        }
        String path = classpath + name.replaceAll("\\.", "/");
        byte[] bytes;

        try (InputStream ins = new FileInputStream(path))
        {
            try(ByteArrayOutputStream out = new ByteArrayOutputStream())
            {
                byte[] buffer = new byte[1024*5];
                int b = 0;
                while ((b = ins.read(buffer)) != -1)
                {
                    out.write(buffer,0,b);
                }
                bytes = out.toByteArray();
            }

        }catch (Exception e)
        {
            throw new ClassNotFoundException();
        }
        return defineClass(name,bytes,0,bytes.length);

    }


    public void loadRouter(ConfigurableApplicationContext context)
    {
        try {
            List actionList = this.getClasses("lz.demo.shop.controller");
            int size = actionList.size();
            for (int i=0;i<size;i++)
            {
                String controllerClass = (String) actionList.get(i);
                Class<?> cls = loadClass(controllerClass);
                Method[] methods = cls.getDeclaredMethods();
                Annotation[] annotationsOfClass = cls.getAnnotations();
                String classUri = "";
                if(annotationsOfClass != null)
                {
                    for (Annotation annotation: annotationsOfClass)
                    {
                        if(annotation.annotationType() == RequestMapping.class)
                        {
                            RequestMapping requestMapping = (RequestMapping) annotation;
                            classUri = requestMapping.value();
                        }
                    }
                }
                for(Method invokeMethod : methods)
                {
                    Annotation[] annotations = invokeMethod.getAnnotations();
                    for(Annotation annotation : annotations)
                    {
                        if(annotation.annotationType() == RequestMapping.class)
                        {
                            RequestMapping requestMapping = (RequestMapping) annotation;
                            String uri = classUri + requestMapping.value();
                            String httpMethod = requestMapping.method().toUpperCase();
                            if(!controllersBeans.containsKey(cls.getName()))
                            {
                                String name = cls.getName();
                                int index = name.lastIndexOf('.');
                                String firstChar = name.substring(index+1,index+2).toLowerCase();
                                String className = firstChar + name.substring(index+2);
                                Object controller = context.getBean(className);
                                controllersBeans.put(cls.getName(),controller);
                            }
                            Action action = new Action(controllersBeans.get(cls.getName()),invokeMethod);

                            Class[] params = invokeMethod.getParameterTypes();
                            if(params.length == 1 && params[0] == FullHttpRequest.class) {
                                action.setInjectionFullhttprequest(true);
                            }

                            httpRouterAction.put(new HttpLabel(uri,new HttpMethod(httpMethod)),action);
                        }

                    }
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Action getRoute(HttpLabel httpLabel)
    {
        return httpRouterAction.get(httpLabel);
    }

    public List getClasses(String packName) throws Exception{
        List actionList = new ArrayList();

        String pkDirName = packName.replace('.','/');
        Enumeration<URL> urls = this.getClass().getClassLoader().getResources(pkDirName);
        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();
            String protocol = url.getProtocol();
            if("file".equals(protocol))
            {
                String filePath = URLDecoder.decode(url.getFile(),"UTF-8");
                findClassByFile(packName,filePath,actionList);
            }else if ("jar".equals(protocol))
            {
                JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                findClassByJar(packName,jar,actionList);
            }
        }
        return actionList;
    }


    private void findClassByFile(String pkgName,String pkgPath,List actionList)
    {
        File dir = new File(pkgPath);
        if(!dir.exists() || !dir.isDirectory())
        {
            return;
        }
        File[] dirfiles = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith("class"));
        if(dirfiles == null || dirfiles.length == 0)
        {
            return;
        }

        for (File f :dirfiles)
        {
            if(f.isDirectory()){
                findClassByFile(pkgName + "." + f.getName() , pkgPath + "/" + f.getName(), actionList);
                continue;
            }

            String className = f.getName();
            if(className.endsWith("Controller.class"))
            {
                className = className.substring(0,className.lastIndexOf("."));
                actionList.add(pkgName + "." + className);
            }
        }
    }

    private void findClassByJar(String pkgName,JarFile jar,List actionList) throws Exception
    {
        String pkgDir = pkgName.replace(".","/");

        Enumeration<JarEntry> entry = jar.entries();
        JarEntry jarEntry;
        while (entry.hasMoreElements())
        {
            jarEntry = entry.nextElement();
            String name = jarEntry.getName();
            if(name.charAt(0) == '/')
            {
                name = name.substring(1);
            }

            if(jarEntry.isDirectory() || !name.startsWith(pkgDir) || !name.endsWith("Controller.class"))
            {
                continue;
            }

            name = name.substring(0,name.lastIndexOf("."));
            name = name.replace("/",".");
            actionList.add(name);
        }
    }

}
