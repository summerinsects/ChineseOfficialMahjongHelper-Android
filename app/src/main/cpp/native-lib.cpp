#include <jni.h>
#include <string>
#include <vector>
#include "mahjong-algorithm/stringify.h"
#include "mahjong-algorithm/fan_calculator.h"
#include "mahjong-algorithm/shanten.h"

extern "C" JNIEXPORT jstring

JNICALL
Java_net_tziakcha_chineseofficialmahjonghelper_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    return env->NewStringUTF("Built " __DATE__ " " __TIME__);
}

extern "C"
JNIEXPORT jint JNICALL
Java_net_tziakcha_chineseofficialmahjonghelper_Mahjong_calculateFan(
        JNIEnv *env, jclass clazz,
        jintArray st, jintArray fp, jint wt, jint cond, jint pw, jint sw, jint flw, jintArray ft) {
    mahjong::calculate_param_t param{};

    param.hand_tiles.pack_count = env->GetArrayLength(fp);
    auto &fixed_packs = param.hand_tiles.fixed_packs;
    jint *fpArray = env->GetIntArrayElements(fp, nullptr);
    for (std::intptr_t i = 0, c = param.hand_tiles.pack_count; i < c; ++i) {
        fixed_packs[i] = static_cast<mahjong::pack_t>(fpArray[i]);
    }
    env->ReleaseIntArrayElements(fp, fpArray, JNI_COMMIT);

    param.hand_tiles.tile_count = env->GetArrayLength(st);
    auto &standing_tiles = param.hand_tiles.standing_tiles;
    jint *stArray = env->GetIntArrayElements(st, nullptr);
    for (std::intptr_t i = 0, c = param.hand_tiles.tile_count; i < c; ++i) {
        standing_tiles[i] = static_cast<mahjong::tile_t>(stArray[i]);
    }
    env->ReleaseIntArrayElements(st, stArray, JNI_COMMIT);

    param.win_tile = static_cast<mahjong::tile_t>(wt);
    param.flower_count = static_cast<std::uint8_t>(flw);
    param.win_flag = static_cast<mahjong::win_flag_t>(cond);
    param.prevalent_wind = static_cast<mahjong::wind_t>(pw);
    param.seat_wind = static_cast<mahjong::wind_t>(sw);

    mahjong::fan_table_t table{};
    int res = mahjong::calculate_fan(&param, &table);
    if (res > 0) {
        jsize length = env->GetArrayLength(ft);
        jint *ftArray = env->GetIntArrayElements(ft, nullptr);
        for (std::intptr_t i = 0, c = std::min<std::intptr_t>(length, mahjong::FAN_TABLE_SIZE); i < c; ++i) {
            ftArray[i] = table[i];
        }
        env->ReleaseIntArrayElements(ft, ftArray, JNI_COMMIT);
    }

    return res;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_net_tziakcha_chineseofficialmahjonghelper_Mahjong_enumDiscardTile(
        JNIEnv *env, jclass clazz,
        jintArray st, jint wt) {

    mahjong::hand_tiles_t hand_tiles{};
    hand_tiles.tile_count = env->GetArrayLength(st);
    auto &standing_tiles = hand_tiles.standing_tiles;
    jint *stArray = env->GetIntArrayElements(st, nullptr);
    for (std::intptr_t i = 0, c = hand_tiles.tile_count; i < c; ++i) {
        standing_tiles[i] = static_cast<mahjong::tile_t>(stArray[i]);
    }
    env->ReleaseIntArrayElements(st, stArray, JNI_COMMIT);

    std::vector<mahjong::enum_result_t> results;
    mahjong::enum_discard_tile(&hand_tiles, static_cast<mahjong::tile_t>(wt), FORM_FLAG_ALL, &results, [](void *context, const mahjong::enum_result_t *result){
        if (result->shanten != std::numeric_limits<int>::max()) {
            reinterpret_cast<std::vector<mahjong::enum_result_t> *>(context)->push_back(*result);
        }
        return true;
    });

    if (results.empty()) {
        return nullptr;
    }

    static const auto convert_useful_table = [](const mahjong::useful_table_t &useful_table) {
        uint64_t res = 0;
        const auto &all_tiles = mahjong::standard_tiles<>::all;
        for (unsigned i = 0; i < 34; ++i) {
            if (useful_table[all_tiles[i]]) {
                res |= (1LL << i);
            }
        }
        return (jlong)res;
    };

    jclass cls = env->FindClass("net/tziakcha/chineseofficialmahjonghelper/Mahjong$EnumResult");
    jmethodID ctor = env->GetMethodID(cls, "<init>", "()V");
    jfieldID fDiscard = env->GetFieldID(cls, "discard", "I");
    jfieldID fForm = env->GetFieldID(cls, "form", "I");
    jfieldID fShanten = env->GetFieldID(cls, "shanten", "I");
    jfieldID fUseful = env->GetFieldID(cls, "useful", "J");
    jobjectArray objArr = env->NewObjectArray(results.size(), cls, nullptr);
    for (std::size_t i = 0, size = results.size(); i < size; ++i) {
        auto &res = results[i];

        jobject obj = env->NewObject(cls, ctor);
        env->SetIntField(obj, fDiscard, res.discard_tile);
        env->SetIntField(obj, fForm, res.form_flag);
        env->SetIntField(obj, fShanten, res.shanten);
        env->SetLongField(obj, fUseful, convert_useful_table(res.useful_table));

        env->SetObjectArrayElement(objArr, i, obj);
    }

    return objArr;
}
