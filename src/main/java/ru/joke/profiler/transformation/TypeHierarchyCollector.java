package ru.joke.profiler.transformation;

import ru.joke.profiler.ProfilerAgent;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.joke.profiler.util.ArgUtil.checkNotEmpty;
import static ru.joke.profiler.util.BytecodeUtil.OBJECT_TYPE;

final class TypeHierarchyCollector {

    private static final Logger logger = Logger.getLogger(ProfilerAgent.class.getCanonicalName());

    private static final String CLASS_EXT = ".class";

    private final Map<String, String> type2superTypeMap;

    TypeHierarchyCollector() {
        this.type2superTypeMap = new ConcurrentHashMap<>(4096, 0.5f, 256);
    }

    List<String> collect(final String type) {
        final List<String> result = new ArrayList<>(4);
        String currentType = checkNotEmpty(type, "type");
        result.add(currentType);
        while (true) {
            final String superType = this.type2superTypeMap.computeIfAbsent(currentType, this::readSuperType);
            result.add(superType);
            if (OBJECT_TYPE.equals(superType) || Objects.equals(currentType, superType)) {
                break;
            }

            currentType = superType;
        }

        return result;
    }

    private String readSuperType(final String className) {

        final InputStream classStream = ClassLoader.getSystemResourceAsStream(className + CLASS_EXT);
        if (classStream == null) {
            return OBJECT_TYPE;
        }

        try (final DataInputStream dis = new DataInputStream(classStream)) {

            /* magic word (4) + minor version (2) + major version (2) */
            dis.skipBytes(4 + 2 + 2);

            /* 2 byte to constants pool size */
            final int constantPoolCount = dis.readUnsignedShort();
            final String[] constantPool = readConstantPool(dis, constantPoolCount);

            /* access_flags (2) + this_class (2) */
            dis.skipBytes(2 + 2);

            /* 2 bytes to super_class info */
            final int superClassIndex = dis.readUnsignedShort();
            if (superClassIndex == 0){
                return OBJECT_TYPE;
            }

            final String superClassTag = constantPool[superClassIndex];
            if (superClassTag == null) {
                return null;
            }

            final int superClassIndexInPoolTable = Integer.parseInt(superClassTag);
            final String superType = constantPool[superClassIndexInPoolTable];
            return superType == null || superType.isEmpty() ? OBJECT_TYPE : superType;
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("Unable to find super type of class: %s", className), e);
            return OBJECT_TYPE;
        }
    }

    private String[] readConstantPool(
            final DataInputStream dataInputStream,
            final int constantPoolCount
    ) throws IOException {
        final String[] result = new String[constantPoolCount];
        for (int i = 1; i < constantPoolCount; i++) {
            /* 1 byte to constant type tag */
            final int tag = dataInputStream.readUnsignedByte();
            switch (tag) {
                /*
                 * 7 = CONSTANT_Class (2-byte data)
                 */
                case 7:
                    final int classIndex = dataInputStream.readUnsignedShort();
                    result[i] = String.valueOf(classIndex);
                    break;
                /*
                 * Constant types with 2-byte data
                 * ===============================
                 * 7 = CONSTANT_Class
                 * 8 = CONSTANT_String
                 * 16 = CONSTANT_MethodType_info
                 * 19 = CONSTANT_Module_info
                 * 20 = CONSTANT_Package_info
                 */
                case 8:
                case 16:
                case 19:
                case 20: {
                    dataInputStream.skipBytes(2);
                    break;
                }
                /*
                 * Constant types with 4-byte data
                 * ===============================
                 * 3 = CONSTANT_Integer
                 * 4 = CONSTANT_Float
                 * 9 = CONSTANT_Fieldref
                 * 10 = CONSTANT_Methodref
                 * 11 = CONSTANT_InterfaceMethodref
                 * 12 = CONSTANT_NameAndType
                 * 17 = CONSTANT_Dynamic
                 * 18 = CONSTANT_InvokeDynamic
                 */
                case 3:
                case 4:
                case 9:
                case 10:
                case 11:
                case 12:
                case 17:
                case 18: {
                    dataInputStream.skipBytes(4);
                    break;
                }
                /*
                 * Constant types with 8-byte data
                 * ===============================
                 * 5 = CONSTANT_Long
                 * 6 = CONSTANT_Double
                 */
                case 5:
                case 6: {
                    dataInputStream.skipBytes(8);
                    i++;
                    break;
                }
                /*
                 * Constant UTF8 type (floating length)
                 * ====================================
                 * 5 = CONSTANT_Utf8
                 */
                case 1:
                    final int length = dataInputStream.readUnsignedShort();
                    final byte[] bytes = new byte[length];
                    dataInputStream.readFully(bytes);
                    result[i] = new String(bytes, StandardCharsets.UTF_8);
                    break;
                /*
                 * Constant with 3-byte length
                 * ===============================
                 * 15 = CONSTANT_MethodDescriptor_info
                 */
                case 15:
                    dataInputStream.skipBytes(3);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported constant type: " + tag);
            }
        }

        return result;
    }
}
