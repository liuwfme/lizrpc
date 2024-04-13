package cn.liz.lizrpc.core.cluster;

import cn.liz.lizrpc.core.api.Router;
import cn.liz.lizrpc.core.meta.InstanceMeta;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Data
public class GrayRouter implements Router<InstanceMeta> {

    private int grayRatio;

    private Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        if (providers == null || providers.size() <= 1) return providers;
        List<InstanceMeta> normalInstances = new ArrayList<>();
        List<InstanceMeta> grayInstances = new ArrayList<>();
        providers.forEach(p -> {
            if ("true".equals(p.getParameters().get("gray"))) {
                grayInstances.add(p);
            } else {
                normalInstances.add(p);
            }
        });

        log.info("GrayRouter.route, gray:{}, normal:{}, grayRatio:{}",
                grayInstances.size(), normalInstances.size(), grayRatio);
        if (grayInstances.isEmpty() || normalInstances.isEmpty()) return providers;

        if (random.nextInt(100) < grayRatio) {
            log.info("GrayRouter.route, grayInstances:{}", grayInstances);
            return grayInstances;
        } else {
            log.info("GrayRouter.route, normalInstances:{}", normalInstances);
            return normalInstances;
        }
    }
}
