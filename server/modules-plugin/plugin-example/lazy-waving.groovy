import org.openwes.api.platform.api.constants.CallbackApiTypeEnum
import org.openwes.common.utils.http.Response
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO

class LazyWaving {

    List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> orders, Object ctx) {
        if (!orders) {
            return []
        }
        def apiCode = ctx.configString('packingStatusApiCode') ?: 'GET_PACKING_STATUS'
        def onlineValue = ctx.configString('packingStatusOnlineValue') ?: '1'
        ctx.log().info('lazy-waving: received {} orders for plugin {}', orders.size(), ctx.pluginKey())

        def waveNos = orders.findAll { it.customerWaveNo }.collect { it.customerWaveNo } as Set
        def related = waveNos ? ctx.outbound().getByCustomerWaveNos(waveNos) : []
        def relatedByWaveNo = related.groupBy { it.customerWaveNo }

        def pending = orders.findAll { order ->
            if (!order.customerWaveNo) {
                return true
            }
            def group = relatedByWaveNo[order.customerWaveNo] ?: []
            group.every {
                it.outboundPlanOrderStatus == OutboundPlanOrderStatusEnum.NEW ||
                    it.outboundPlanOrderStatus == OutboundPlanOrderStatusEnum.ASSIGNED
            }
        }

        def selected = pending
        if (pending.size() != orders.size()) {
            boolean online = packingStationOnline(apiCode, onlineValue, ctx)
            selected = online ? orders : pending
            ctx.log().info('lazy-waving: packing station online={}, selected {} of {} orders',
                online, selected.size(), orders.size())
        }
        return group(selected)
    }

    private boolean packingStationOnline(String apiCode, String onlineValue, Object ctx) {
        Response response = ctx.callback().callback(CallbackApiTypeEnum.COMMON_CALLBACK, apiCode, null)
        if (response == null || response.code != Response.SUCCESS_CODE) {
            ctx.log().warn('lazy-waving: packing-station status unavailable, response={}', response)
            return false
        }
        return response.data == onlineValue
    }

    private List<List<OutboundPlanOrderDTO>> group(List<OutboundPlanOrderDTO> orders) {
        List<List<OutboundPlanOrderDTO>> result = []
        result.addAll(orders.findAll { it.customerWaveNo }.groupBy { it.customerWaveNo }.values())
        result.addAll(orders.findAll { !it.customerWaveNo }.groupBy { it.customerOrderType ?: '' }.values())
        return result
    }
}
