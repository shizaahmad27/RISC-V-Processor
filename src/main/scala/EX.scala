package FiveStage
import chisel3._
import chisel3.util.{ MuxLookup }
import chisel3.experimental.MultiIOModule

class Execute extends MultiIOModule {
    val io = IO(
        new Bundle {
            val op1 = Input(UInt(32.W))
            val op2 = Input(UInt(32.W))
            val aluOp = Input(UInt(4.W))
            val aluResult = Output(UInt(32.W))

            val branchType = Input(UInt(3.W))
            val branch = Input(Bool())
            val jump = Input(Bool())
            val immediate = Input(UInt(32.W))
            val PCout = Input(UInt(32.W))

            val branchTaken = Output(Bool())
            val branchTarget = Output(UInt(32.W))

            // Milestone 3. Needed for MEM stage forwarding

            val memAluResult = Input(UInt(32.W))
            val memRegWrite = Input(Bool())
            val memRd = Input(UInt(5.W))

            val wbData = Input(UInt(32.W))
            val wbRegWrite = Input(Bool())
            val wbRegRd = Input(UInt(5.W))

            val rs1 = Input(UInt(5.W))
            val rs2 = Input(UInt(5.W))
            val op1IsRegister = Input(Bool())
            val op2IsRegister = Input(Bool())
        }
    )

    val forwardedop1 = Mux(io.op1IsRegister, Mux(io.memRegWrite && io.memRd === io.rs1 && io.memRd =/= 0.U, io.memAluResult, Mux(io.wbRegWrite && io.wbRegRd === io.rs1 && io.wbRegRd =/= 0.U, io.wbData, io.op1)), io.op1)
    val forwardedop2 = Mux(io.op2IsRegister, Mux(io.memRegWrite && io.memRd === io.rs2 && io.memRd =/= 0.U, io.memAluResult, Mux(io.wbRegWrite && io.wbRegRd === io.rs2 && io.wbRegRd =/= 0.U, io.wbData, io.op2)), io.op2)

    val alu = Module(new ALU).io
    alu.op1 := forwardedop1
    alu.op2 := forwardedop2
    alu.aluOp := io.aluOp
    val comparison = MuxLookup(io.branchType, false.B, Array(
        branchType.beq  -> (forwardedop1 === forwardedop2),
        branchType.neq  -> (forwardedop1 =/= forwardedop2),
        branchType.lt   -> (forwardedop1.asSInt < forwardedop2.asSInt),
        branchType.gte  -> (forwardedop1.asSInt >= forwardedop2.asSInt),
        branchType.ltu  -> (forwardedop1 < forwardedop2),
        branchType.gteu -> (forwardedop1 >= forwardedop2),
    ))

   

    io.branchTaken  := io.jump || (io.branch && comparison)
    io.branchTarget := Mux(io.branch, io.PCout + io.immediate, alu.aluResult)
    io.aluResult    := Mux(io.jump, io.PCout + 4.U, alu.aluResult)
}
