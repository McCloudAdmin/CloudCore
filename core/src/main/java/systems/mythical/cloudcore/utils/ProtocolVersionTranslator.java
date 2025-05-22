package systems.mythical.cloudcore.utils;

import java.util.HashMap;
import java.util.Map;

public class ProtocolVersionTranslator {
    private static final Map<Integer, String> protocolMap = new HashMap<>();
    static {
        // 1.21.x
        protocolMap.put(770, "1.21.5");
        protocolMap.put(769, "1.21.4");
        protocolMap.put(768, "1.21.2/1.21.3");
        protocolMap.put(767, "1.21/1.21.1");
        
        // 1.20.6
        protocolMap.put(766, "1.20.6");
        
        // 1.20.5
        protocolMap.put(765, "1.20.5");
        
        // 1.20.4
        protocolMap.put(764, "1.20.4");
        
        // 1.20.3
        protocolMap.put(763, "1.20.3");
        
        // 1.20.2
        protocolMap.put(762, "1.20.2");
        protocolMap.put(0x40000099, "1.20.2-RC2");
        protocolMap.put(0x40000098, "1.20.2-RC1");
        protocolMap.put(0x40000097, "1.20.2-PR4");
        protocolMap.put(0x40000096, "1.20.2-PR3");
        protocolMap.put(0x40000095, "1.20.2-PR2");
        protocolMap.put(0x40000094, "1.20.2-PR1");
        
        // 1.20.1
        protocolMap.put(761, "1.20.1");
        protocolMap.put(0x4000008E, "1.20.1-RC1");
        
        // 1.20
        protocolMap.put(760, "1.20");
        protocolMap.put(0x4000008D, "1.20-RC1");
        protocolMap.put(0x4000008C, "1.20-PR7");
        protocolMap.put(0x4000008B, "1.20-PR6");
        protocolMap.put(0x4000008A, "1.20-PR5");
        protocolMap.put(0x40000089, "1.20-PR4");
        protocolMap.put(0x40000088, "1.20-PR3");
        protocolMap.put(0x40000087, "1.20-PR2");
        protocolMap.put(0x40000086, "1.20-PR1");
        
        // 1.19.4
        protocolMap.put(759, "1.19.4");
        protocolMap.put(0x4000007E, "1.19.4-RC3");
        protocolMap.put(0x4000007D, "1.19.4-RC2");
        protocolMap.put(0x4000007C, "1.19.4-RC1");
        protocolMap.put(0x4000007B, "1.19.4-PR4");
        protocolMap.put(0x4000007A, "1.19.4-PR3");
        protocolMap.put(0x40000079, "1.19.4-PR2");
        protocolMap.put(0x40000078, "1.19.4-PR1");

        // 1.19.3
        protocolMap.put(758, "1.19.3");
        protocolMap.put(0x40000072, "1.19.3-RC3");
        protocolMap.put(0x40000071, "1.19.3-RC2");
        protocolMap.put(0x40000070, "1.19.3-RC1");
        protocolMap.put(0x4000006F, "1.19.3-PR3");
        protocolMap.put(0x4000006E, "1.19.3-PR2");
        protocolMap.put(0x4000006D, "1.19.3-PR1");

        // 1.19.2
        protocolMap.put(757, "1.19.2");
        protocolMap.put(0x40000067, "1.19.2-RC2");
        protocolMap.put(0x40000066, "1.19.2-RC1");

        // 1.19.1
        protocolMap.put(756, "1.19.1");
        protocolMap.put(0x40000065, "1.19.1-RC3");
        protocolMap.put(0x40000064, "1.19.1-RC2");
        protocolMap.put(0x40000063, "1.19.1-PR6");
        protocolMap.put(0x40000062, "1.19.1-PR5");
        protocolMap.put(0x40000061, "1.19.1-PR4");
        protocolMap.put(0x40000060, "1.19.1-PR3");
        protocolMap.put(0x4000005F, "1.19.1-PR2");
        protocolMap.put(0x4000005E, "1.19.1-RC1");
        protocolMap.put(0x4000005D, "1.19.1-PR1");

        // 1.19
        protocolMap.put(755, "1.19");
        protocolMap.put(0x4000005B, "1.19-RC2");
        protocolMap.put(0x4000005A, "1.19-RC1");
        protocolMap.put(0x40000059, "1.19-PR5");
        protocolMap.put(0x40000058, "1.19-PR4");
        protocolMap.put(0x40000057, "1.19-PR3");
        protocolMap.put(0x40000056, "1.19-PR2");
        protocolMap.put(0x40000055, "1.19-PR1");

        // 1.18.2
        protocolMap.put(754, "1.18.2");
        protocolMap.put(0x40000049, "1.18.2-RC1");
        protocolMap.put(0x40000048, "1.18.2-PR3");
        protocolMap.put(0x40000047, "1.18.2-PR2");
        protocolMap.put(0x40000046, "1.18.2-PR1");

        // 1.18.1
        protocolMap.put(753, "1.18.1");
        protocolMap.put(0x40000040, "1.18.1-RC3");
        protocolMap.put(0x4000003F, "1.18.1-RC2");
        protocolMap.put(0x4000003E, "1.18.1-RC1");
        protocolMap.put(0x4000003D, "1.18.1-PR1");

        // 1.18
        protocolMap.put(752, "1.18");
        protocolMap.put(0x4000003C, "1.18-RC4");
        protocolMap.put(0x4000003B, "1.18-RC3");
        protocolMap.put(0x4000003A, "1.18-RC2");
        protocolMap.put(0x40000039, "1.18-RC1");
        protocolMap.put(0x40000038, "1.18-PR8");
        protocolMap.put(0x40000037, "1.18-PR7");
        protocolMap.put(0x40000036, "1.18-PR6");
        protocolMap.put(0x40000035, "1.18-PR5");
        protocolMap.put(0x40000034, "1.18-PR4");
        protocolMap.put(0x40000033, "1.18-PR3");
        protocolMap.put(0x40000032, "1.18-PR2");
        protocolMap.put(0x40000031, "1.18-PR1");

        // 1.17.1
        protocolMap.put(751, "1.17.1");
        protocolMap.put(0x40000028, "1.17.1-RC2");
        protocolMap.put(0x40000027, "1.17.1-RC1");
        protocolMap.put(0x40000026, "1.17.1-PR3");
        protocolMap.put(0x40000025, "1.17.1-PR2");
        protocolMap.put(0x40000024, "1.17.1-PR1");

        // 1.17
        protocolMap.put(750, "1.17");
        protocolMap.put(0x40000023, "1.17-RC2");
        protocolMap.put(0x40000022, "1.17-RC1");
        protocolMap.put(0x40000021, "1.17-PR5");
        protocolMap.put(0x40000020, "1.17-PR4");
        protocolMap.put(0x4000001F, "1.17-PR3");
        protocolMap.put(0x4000001E, "1.17-PR2");
        protocolMap.put(0x4000001D, "1.17-PR1");

        // 1.16.5
        protocolMap.put(749, "1.16.5");
        protocolMap.put(0x4000000A, "1.16.5-RC1");

        // 1.16.4
        protocolMap.put(748, "1.16.4");
        protocolMap.put(0x40000003, "1.16.4-RC1");
        protocolMap.put(0x40000002, "1.16.4-PR2");
        protocolMap.put(0x40000001, "1.16.4-PR1");

        // 1.16.3
        protocolMap.put(747, "1.16.3");
        protocolMap.put(746, "1.16.3-RC1");

        // 1.16.2
        protocolMap.put(745, "1.16.2");
        protocolMap.put(744, "1.16.2-RC2");
        protocolMap.put(743, "1.16.2-RC1");
        protocolMap.put(742, "1.16.2-PR3");
        protocolMap.put(741, "1.16.2-PR2");
        protocolMap.put(740, "1.16.2-PR1");

        // 1.16.1
        protocolMap.put(736, "1.16.1");

        // 1.16
        protocolMap.put(735, "1.16");
        protocolMap.put(734, "1.16-RC1");
        protocolMap.put(733, "1.16-PR8");
        protocolMap.put(732, "1.16-PR7");
        protocolMap.put(731, "1.16-PR6");
        protocolMap.put(730, "1.16-PR5");
        protocolMap.put(729, "1.16-PR4");
        protocolMap.put(728, "1.16-PR3");
        protocolMap.put(727, "1.16-PR2");
        protocolMap.put(726, "1.16-PR1");

        // 1.15.2
        protocolMap.put(578, "1.15.2");
        protocolMap.put(577, "1.15.2-PR2");
        protocolMap.put(576, "1.15.2-PR1");

        // 1.15.1
        protocolMap.put(575, "1.15.1");
        protocolMap.put(574, "1.15.1-PR1");

        // 1.15
        protocolMap.put(573, "1.15");
        protocolMap.put(572, "1.15-PR7");
        protocolMap.put(571, "1.15-PR6");
        protocolMap.put(570, "1.15-PR5");
        protocolMap.put(569, "1.15-PR4");
        protocolMap.put(568, "1.15-PR3");
        protocolMap.put(567, "1.15-PR2");
        protocolMap.put(566, "1.15-PR1");

        // 1.14.4
        protocolMap.put(498, "1.14.4");
        protocolMap.put(497, "1.14.4-PR7");
        protocolMap.put(496, "1.14.4-PR6");
        protocolMap.put(495, "1.14.4-PR5");
        protocolMap.put(494, "1.14.4-PR4");
        protocolMap.put(493, "1.14.4-PR3");
        protocolMap.put(492, "1.14.4-PR2");
        protocolMap.put(491, "1.14.4-PR1");

        // 1.14.3
        protocolMap.put(490, "1.14.3");
        protocolMap.put(489, "1.14.3-PR4");
        protocolMap.put(488, "1.14.3-PR3");
        protocolMap.put(487, "1.14.3-PR2");
        protocolMap.put(486, "1.14.3-PR1");

        // 1.14.2
        protocolMap.put(485, "1.14.2");
        protocolMap.put(484, "1.14.2-PR4");
        protocolMap.put(483, "1.14.2-PR3");
        protocolMap.put(482, "1.14.2-PR2");
        protocolMap.put(481, "1.14.2-PR1");

        // 1.14.1
        protocolMap.put(480, "1.14.1");
        protocolMap.put(479, "1.14.1-PR2");
        protocolMap.put(478, "1.14.1-PR1");

        // 1.14
        protocolMap.put(477, "1.14");
        protocolMap.put(476, "1.14-PR5");
        protocolMap.put(475, "1.14-PR4");
        protocolMap.put(474, "1.14-PR3");
        protocolMap.put(473, "1.14-PR2");
        protocolMap.put(472, "1.14-PR1");

        // 1.13.2
        protocolMap.put(404, "1.13.2");
        protocolMap.put(403, "1.13.2-PR2");
        protocolMap.put(402, "1.13.2-PR1");

        // 1.13.1
        protocolMap.put(401, "1.13.1");
        protocolMap.put(400, "1.13.1-PR2");
        protocolMap.put(399, "1.13.1-PR1");

        // 1.13
        protocolMap.put(393, "1.13");
        protocolMap.put(392, "1.13-PR10");
        protocolMap.put(391, "1.13-PR9");
        protocolMap.put(390, "1.13-PR8");
        protocolMap.put(389, "1.13-PR7");
        protocolMap.put(388, "1.13-PR6");
        protocolMap.put(387, "1.13-PR5");
        protocolMap.put(386, "1.13-PR4");
        protocolMap.put(385, "1.13-PR3");
        protocolMap.put(384, "1.13-PR2");
        protocolMap.put(383, "1.13-PR1");

        // 1.12.2
        protocolMap.put(340, "1.12.2");
        protocolMap.put(339, "1.12.2-PR2");
        protocolMap.put(338, "1.12.2-PR1");

        // 1.12.1
        protocolMap.put(338, "1.12.1");
        protocolMap.put(337, "1.12.1-PR1");

        // 1.12
        protocolMap.put(335, "1.12");
        protocolMap.put(334, "1.12-PR7");
        protocolMap.put(333, "1.12-PR6");
        protocolMap.put(332, "1.12-PR5");
        protocolMap.put(331, "1.12-PR4");
        protocolMap.put(330, "1.12-PR3");
        protocolMap.put(329, "1.12-PR2");
        protocolMap.put(328, "1.12-PR1");

        // 1.11.2/1.11.1
        protocolMap.put(316, "1.11.2/1.11.1");

        // 1.11
        protocolMap.put(315, "1.11");
        protocolMap.put(314, "1.11-PR1");

        // 1.10.2/1.10.1/1.10
        protocolMap.put(210, "1.10.X");
        protocolMap.put(205, "1.10-PR2");
        protocolMap.put(204, "1.10-PR1");

        // 1.9.4/1.9.3
        protocolMap.put(110, "1.9.4/1.9.3");
        protocolMap.put(109, "1.9.3-PR3/1.9.3-PR2/1.9.3-PR1/1.9.2");

        // 1.9.1
        protocolMap.put(108, "1.9.1");
        protocolMap.put(107, "1.9.1-PR3/1.9.1-PR2/1.9.1-PR1/1.9");

        // 1.9
        protocolMap.put(106, "1.9-PR4");
        protocolMap.put(105, "1.9-PR3");
        protocolMap.put(104, "1.9-PR2");
        protocolMap.put(103, "1.9-PR1");

        // 1.8.X
        protocolMap.put(47, "1.8.X");
        protocolMap.put(46, "1.8-PR3");
        protocolMap.put(45, "1.8-PR2");
        protocolMap.put(44, "1.8-PR1");

        // 1.7.X
        protocolMap.put(5, "1.7.X");
        protocolMap.put(4, "1.7.1/1.7");
    }

    /**
     * Translate protocol version to string
     * 
     * @param prot Protocol version
     * 
     * @return String
     */
    public static String translateProtocolToString(int prot) {
        if (prot > 770) {
            return "1.21.5+";
        }
        return protocolMap.getOrDefault(prot, "UNKNOWN");
    }
}
