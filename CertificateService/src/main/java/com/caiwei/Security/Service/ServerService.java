package com.caiwei.Security.Service;

import com.caiwei.Security.Entities.User;
import com.caiwei.Security.Entities.WealthRequest;
import com.caiwei.Security.Mapper.UserMapper;
import com.caiwei.Security.Util.RASUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyPair;

/**
 * 执行数据库操作
 */
@Slf4j
public class ServerService {
    /**
     * 初始化mybatis
     */
    static UserMapper userMapper = null;

    static SqlSession sqlSession = null;

    static {

        try {
            System.out.println("use mybatis connect to database ... ");
            // 根据 mybatis-config.xml 配置的信息得到 sqlSessionFactory
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            // 然后根据 sqlSessionFactory 得到 session
            sqlSession = sqlSessionFactory.openSession();
            userMapper = sqlSession.getMapper(UserMapper.class);
        } catch (IOException e) {
            System.out.println("mybatis 初始化错误！");
        }

    }

    public String addOrCompare(WealthRequest wealthRequest, Socket socket, KeyPair keyPair) throws Exception {
        String name = wealthRequest.getName();
        String wealthEncryption = wealthRequest.getWealthEncryption();

        String IPport = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        User user = new User();
        user.setName(IPport + "-" + name);
        user.setAddress(IPport);
        user.setWealthEncryption(wealthEncryption);
        log.info("selfUser = " + user);

        userMapper.addUser(user);
        sqlSession.commit();

        User otherSide = userMapper.selectOtherSide(IPport);
        log.info("otherSideUser = " + otherSide);
        if (otherSide != null) {
            String otherSideWealthEncryption = otherSide.getWealthEncryption();
            //解密出来
            log.info("开始比较, privateKey = " + keyPair.getPrivate());
            String wealthDecryption = RASUtil.decrypt(wealthEncryption, keyPair.getPrivate());
            String otherSideWealthDecryption = RASUtil.decrypt(otherSideWealthEncryption, keyPair.getPrivate());

            double wealth = Double.parseDouble(wealthDecryption);
            double otherSideWealth = Double.parseDouble(otherSideWealthDecryption);

            log.info("self-wealth = " + wealth);
            log.info("otherSide-wealth = " + otherSideWealth);

            userMapper.updateToDelete();
            sqlSession.commit();
            if(wealth > otherSideWealth){
                return "BIG";
            }else if(wealth < otherSideWealth){
                return "SMALL";
            }else {
                return "EQUATION";
            }
        }
        return null;
    }


}
