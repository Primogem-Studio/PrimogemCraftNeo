local default = require("tacz_default_state_machine")
local wings = {}

function wings.entry(this, context)
    this.wing_progress = 0
    this.wing_timestamp = context:getCurrentTimestamp()
    context:runAnimation("wing_fold", context:getTrack(default.STATIC_TRACK_LINE, default.PARALLEL_TRACK_1), false, PLAY_ONCE_HOLD, 0)
    context:pauseAnimation(context:getTrack(default.STATIC_TRACK_LINE, default.PARALLEL_TRACK_1))
    context:setAnimationProgress(context:getTrack(default.STATIC_TRACK_LINE, default.PARALLEL_TRACK_1), 0, true)
end

function wings.update(this, context)
    local now = context:getCurrentTimestamp()
    local elapsed = math.max(0, now - this.wing_timestamp)
    this.wing_timestamp = now
    local recent = now - context:getLastShootTimestamp()
    local firing = recent >= 0 and recent <= context:getShootInterval() + 100
    local aiming = context:getAimingProgress()
    local target = aiming * (2 / 3) + (1 - aiming) * (firing and 0.5 or 0)
    local duration = target > this.wing_progress and 160 or 350
    local step = elapsed / duration * 0.5
    this.wing_progress = math.max(this.wing_progress - step, math.min(target, this.wing_progress + step))
    context:setAnimationProgress(context:getTrack(default.STATIC_TRACK_LINE, default.PARALLEL_TRACK_1), this.wing_progress, true)
end

local M = setmetatable({}, {__index = default})

function M:states()
    local states = default.states(self)
    table.insert(states, wings)
    return states
end

return M
