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

            // Branches are resolved in ID (fast branch). EX only writes the return address for jumps
            val jump = Input(Bool())
            val PCout = Input(UInt(32.W))

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

            val rs2Data = Input(UInt(32.W))
            val rs2DataOutput = Output(UInt(32.W))
        }
    )

    val forwardedop1 = Mux(io.op1IsRegister, Mux(io.memRegWrite && io.memRd === io.rs1 && io.memRd =/= 0.U, io.memAluResult, Mux(io.wbRegWrite && io.wbRegRd === io.rs1 && io.wbRegRd =/= 0.U, io.wbData, io.op1)), io.op1)
    val forwardedop2 = Mux(io.op2IsRegister, Mux(io.memRegWrite && io.memRd === io.rs2 && io.memRd =/= 0.U, io.memAluResult, Mux(io.wbRegWrite && io.wbRegRd === io.rs2 && io.wbRegRd =/= 0.U, io.wbData, io.op2)), io.op2)

    val forwardedRs2Data = Mux(io.memRegWrite && io.memRd === io.rs2 && io.memRd =/= 0.U, io.memAluResult, Mux(io.wbRegWrite && io.wbRegRd === io.rs2 && io.wbRegRd =/= 0.U, io.wbData, io.rs2Data))

    val alu = Module(new ALU).io
    alu.op1 := forwardedop1
    alu.op2 := forwardedop2
    alu.aluOp := io.aluOp

    io.aluResult    := Mux(io.jump, io.PCout + 4.U, alu.aluResult)
    io.rs2DataOutput := forwardedRs2Data
}
